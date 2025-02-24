package com.readyauction.app.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmDto {
    Long productId;
    String productName;
    Integer payPrice;
    Timestamp payTime;
}
