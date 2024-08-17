package com.demo.myshop.dto;

import com.demo.myshop.model.Product;
import jakarta.validation.Valid;
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

    public Product toProduct() {
        return new Product(title, description, category, price, stock, imageUrl);
    }
}