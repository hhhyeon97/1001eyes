package com.demo.productservice.dto;

import com.demo.productservice.model.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDto {
    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final Integer price;
    private final Integer stock;
    private final String imageUrl;

    // Product 엔티티를 DTO로 변환하는 생성자
    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.imageUrl = product.getImageUrl();
    }
}