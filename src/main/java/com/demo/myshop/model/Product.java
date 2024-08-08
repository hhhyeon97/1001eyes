package com.demo.myshop.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, length = 30)
    private String title;

    @Lob
    private String description;

    private int price;

    private String imageUrl;

    private int stock;

    @Column(length = 30)
    private String category;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = new Date();
        }
    }

}
