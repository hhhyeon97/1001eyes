package com.demo.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartCreateRequestDto {
    private Long userId;

    public CartCreateRequestDto(Long userId) {
        this.userId = userId;
    }
}
