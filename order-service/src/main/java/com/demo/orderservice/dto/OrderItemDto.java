package com.demo.orderservice.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OrderItemDto {
    private Long productId;
    private Integer quantity;

    private String productName;
    private Integer price;
}

