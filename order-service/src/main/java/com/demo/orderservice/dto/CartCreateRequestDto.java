package com.demo.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartCreateRequestDto {
    private String userId;

    public CartCreateRequestDto(String userId) {
        this.userId = userId;
    }

}