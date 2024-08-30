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
    private final Integer stock; //-> 수량 api 따로 !
    private final String imageUrl;

    // Product 엔티티를 DTO로 변환하는 생성자
    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.stock = product.getStock(); // todo : 상품 목록 조회하면 db 재고로 나오는데 어차피 리스트에선 재고 보여주지 않을 거니까 제거하고 싶은데 !
                                        // -> 상품 목록 api용 responseDto를 따로 만든다 ?
                                        // 추후 메인 페이지에서 상품 리스트 보여줄 땐 상품명,상품사진,가격 정도만 보여주기
        this.imageUrl = product.getImageUrl();
    }

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
    }
}