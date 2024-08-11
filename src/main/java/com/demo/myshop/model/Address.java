package com.demo.myshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address; // 기본 주소
    private String addressDetail; // 상세 주소
    private String zipcode; // 우편 번호

    private String name; // 받는 분 이름
    private boolean isDefault; // 기본 배송지 체크

    private String phoneNumber; // 전화번호
    private String message; // 배송 메세지

    @ManyToOne
    @JoinColumn(name = "user_id") // 외래 키 열 이름 명시
    private User user;

}