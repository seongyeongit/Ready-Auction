package com.readyauction.app.inquiry.controller;

import com.readyauction.app.auth.principal.AuthPrincipal;
import com.readyauction.app.inquiry.dto.InquiryAnswerDto;
import com.readyauction.app.inquiry.dto.InquiryDetailDto;
import com.readyauction.app.inquiry.dto.InquiryDto;
import com.readyauction.app.inquiry.dto.InquiryReqDto;
import com.readyauction.app.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/inquiry")
@Slf4j
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;

    @GetMapping("/faq")
    public void inquiry() {

    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("inquiryReqDto", new InquiryReqDto());
        return "inquiry/inquiry-register";
    }

    // 문의 등록하기
    @PostMapping("/register")
    public String registerInquiry(@ModelAttribute InquiryReqDto inquiryReqDto) {
        inquiryService.registerInquiry(inquiryReqDto);
        return "redirect:/mypage";
    }

    // 관리자 문의 조회
    @GetMapping("/admin-faq")
    public String adminInquiry(Model model) {
        List<InquiryDto> inquiryDtos = inquiryService.findAll();
        model.addAttribute("inquiryList", inquiryDtos);
        log.info("inquiryDtos : {}", inquiryDtos);
        return "inquiry/admin-faq";
    }

    // 관리자 문의 상세 조회하기
    @GetMapping("/detail/{id}")
    public String adminInquiryDetail(
            Long id,
            Model model) {
        InquiryDetailDto inquiryDetailDto = inquiryService.findAndNicknameById(id);
        model.addAttribute("inquiry", inquiryDetailDto);
        log.info("inquiryDetailDto : {}", inquiryDetailDto);
        return "inquiry/admin-faq-detail";
    }

    // 관리자 댓글 추가하기
    @PostMapping("/detail/answer")
    public String createInquiryDetailAnswer(
            @ModelAttribute InquiryAnswerDto inquiryAnswerDto,
            @AuthenticationPrincipal AuthPrincipal principal
    ){
        log.info("inquiryAnswerDto : {}", inquiryAnswerDto);
        inquiryService.addAnswer(principal.getMember().getId(), inquiryAnswerDto);
        return "redirect:/inquiry/detail/" + inquiryAnswerDto.getInquiryId();
    }

    // 관리자 댓글 수정하기
    @ResponseBody
    @PatchMapping("/detail/answer")
    public ResponseEntity<?> updateInquiryDetailAnswer(
            @RequestBody InquiryAnswerDto inquiryAnswerDto,
            @AuthenticationPrincipal AuthPrincipal principal
    ){
        log.info("inquiryAnswerDto : {}", inquiryAnswerDto);
        inquiryService.changeAnswer(principal.getMember().getId(), inquiryAnswerDto);
        return ResponseEntity.ok("ok");
    }

    // 댓글 삭제하기 -> 문의 엔티티 수정이라서 patch 사용
    @ResponseBody
    @PatchMapping("/detail/delete/answer")
    public ResponseEntity<?> deleteInquiryDetailAnswer(
            @RequestBody InquiryAnswerDto inquiryAnswerDto,
            @AuthenticationPrincipal AuthPrincipal principal
    ){
        log.info("inquiryAnswerDto : {}", inquiryAnswerDto);
        inquiryService.deleteAnswer(principal.getMember().getId(), inquiryAnswerDto);
        return ResponseEntity.ok("ok");
    }

    // 문의 게시글 조회
    @GetMapping("/{id}")
    public String getInquiryDetail(@PathVariable("id") Long id, Model model) {
        InquiryDetailDto inquiryDetailDto = inquiryService.findAndNicknameById(id);
        log.debug("inquiryDetailDto : {}", inquiryDetailDto);
        model.addAttribute("inquiry", inquiryDetailDto);
        return "inquiry/inquiry-detail";
    }

}
