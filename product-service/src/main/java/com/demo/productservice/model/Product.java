package com.demo.productservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, length = 30)
    private String title;

    @Lob
    private String description;

    private Integer price;

    private String imageUrl;

    private Integer stock;

    @Column(length = 30)
    private String category;

    @CreationTimestamp
    private LocalDateTime created_at;

    // 오픈 시간을 저장하는 필드 추가
    private LocalDateTime openTime;

    public Product(String title, String description, String category, Integer price,
                   Integer stock, String imageUrl, LocalDateTime openTime) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.openTime = openTime;
    }

}
