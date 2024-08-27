package com.demo.orderservice.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {

    private String productId;
    private Integer quantity;
    private Double price;

}
