package com.demo.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PaymentDto {

    private Long paymentId;
    private String userId;
    private List<PaymentRequestDto> paymentItems; // 각 결제 항목의 정보
    private LocalDateTime createdAt;

}
