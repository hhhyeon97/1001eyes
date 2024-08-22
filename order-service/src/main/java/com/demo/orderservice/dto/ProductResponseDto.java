package com.demo.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDto {

    private Long id;
    private String title;
    private String description;
    private String category;
    private Integer price;
    private Integer stock;
    private String imageUrl;
}
