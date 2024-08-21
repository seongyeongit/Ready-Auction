package com.readyauction.app.cash.service;

import com.readyauction.app.auction.service.ProductService;
import com.readyauction.app.cash.dto.TransactionDto;
import com.readyauction.app.cash.entity.Cash;
import com.readyauction.app.cash.entity.Payment;
import com.readyauction.app.cash.entity.PaymentStatus;
import com.readyauction.app.cash.repository.CashRepository;
import com.readyauction.app.cash.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final CashRepository cashRepository;
    private final PaymentRepository paymentRepository;
    private final ProductService productService;

    public List<TransactionDto> getTransactionHistory(Long memberId, Long accountId) {
        List<TransactionDto> transactions = new ArrayList<>();

        // Cash 내역 추가
        List<Cash> cashList = cashRepository.findAllByAccountId(accountId);
        for (Cash cash : cashList) {
            String cashStatus = cash.getStatus() != null ? cash.getStatus().name() : "UNKNOWN";
            transactions.add(new TransactionDto(cash.getDate(), "캐시", cash.getAmount(), "CHARGE".equals(cashStatus) ? "충전" : "출금"));
        }

        // Payment 관련 내역 - 판매자 판매 완료 (입금) 제외
        // memberId와 일치하는 payment 조회
        List<Payment> paymentsByMemberId = paymentRepository.findAllByMemberId(memberId);
        String productName;
        Long productId;

        for (Payment payment : paymentsByMemberId) {
            productName = productService.findProductNameById(payment.getProductId());
            productId = payment.getProductId();

            // 선입금 환불 (입금) 및 출금 내역 보존
            if (payment.getStatus() == PaymentStatus.ROLLBACK_COMPLETED) {
                transactions.add(new TransactionDto(payment.getDate(), "선입금", payment.getPayAmount(), "환불", productName, productId));
                transactions.add(new TransactionDto(payment.getDate(), "선입금", -payment.getPayAmount(), "지불", productName, productId));
            } else if (payment.getStatus() == PaymentStatus.PROCESSING) {
                // 선입금 (출금)
                transactions.add(new TransactionDto(payment.getDate(), "선입금", -payment.getPayAmount(), "지불", productName, productId));
            } else if (payment.getStatus() == PaymentStatus.COMPLETED) {
                // 구매 (출금)
                transactions.add(new TransactionDto(payment.getDate(), "구매", -payment.getPayAmount(), "완료", productName, productId));
            }
        }

        // Payment - 판매자 판매 완료 (입금)
        // receiverAccountId와 일치하는 payment 조회
        List<Payment> paymentsByAccountId = paymentRepository.findAllByReceiverAccountId(accountId);
        for (Payment payment : paymentsByAccountId) {
            productName = productService.findProductNameById(payment.getProductId());
            productId = payment.getProductId();

            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                transactions.add(new TransactionDto(payment.getDate(), "판매", payment.getPayAmount(), "완료", productName, productId));
            }
        }

        // 날짜 기준으로 내림차순 정렬
        return transactions.stream()
                .sorted(Comparator.comparing(TransactionDto::getDate).reversed())
                .collect(Collectors.toList());
    }
}
