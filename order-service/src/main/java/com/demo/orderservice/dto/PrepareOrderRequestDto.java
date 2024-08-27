package com.demo.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PrepareOrderRequestDto {

    private Long productId;
    private Integer quantity;
    // 결제요청dto랑 동일하게 price도 받아두는게 좋을까?
}
