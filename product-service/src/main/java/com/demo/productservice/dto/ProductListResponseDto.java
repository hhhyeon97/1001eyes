package com.demo.productservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductListResponseDto {

    private Long id;
    private String title;
    private String category;
    private Integer price;
    private String imageUrl;

    public ProductListResponseDto(Long id, String title, String category, Integer price, String imageUrl) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}
