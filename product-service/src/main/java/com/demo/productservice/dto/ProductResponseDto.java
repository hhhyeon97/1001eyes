package com.demo.productservice.dto;

import com.demo.productservice.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProductResponseDto {
    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final Integer price;
    private final Integer stock; //-> 수량 api 따로 !
    private final String imageUrl;
    private final LocalDateTime openTime;
    private String errorMessage;  // 에러 메시지 필드 추가


    // 에러 메시지만 전달하는 생성자 추가
    public ProductResponseDto(String errorMessage) {
        this.id = null;
        this.title = null;
        this.description = null;
        this.category = null;
        this.price = null;
        this.stock = null;
        this.imageUrl = null;
        this.openTime = null;
        this.errorMessage = errorMessage;
    }

  /*  // Product 엔티티를 DTO로 변환하는 생성자
    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.imageUrl = product.getImageUrl();
        this.openTime = product.getOpenTime();
    }*/

    // Product 엔티티를 DTO로 변환하는 생성자
    // ++ 상품 상세페이지에서 임시 재고로 보여줄 용도 !!
    public ProductResponseDto(Product product, Integer stock) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.stock = stock;
        this.imageUrl = product.getImageUrl();
        this.openTime = product.getOpenTime();
    }
}