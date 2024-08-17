package com.demo.myshop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDto {
    private String title;
    private String description;
    private String category;
    private Integer price;
    private Integer stock;
    private String imageUrl;
}