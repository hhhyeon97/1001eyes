package com.demo.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PrepareOrderRequestDto {

    private Long productId;
    private Integer quantity;
    private LocalDateTime createTime;

}
