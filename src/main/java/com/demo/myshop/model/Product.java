package com.demo.myshop.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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

}
