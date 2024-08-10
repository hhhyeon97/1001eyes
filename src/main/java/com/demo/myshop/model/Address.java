package com.demo.myshop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    private String address_detail; // 상세 주소
    private String zipcode; // 우편 번호

    private String name; // 받는 분 이름
    private String default_address; // 기본 배송지

    private String phone_number; // 전화번호
    private String message; // 배송 메세지

    @ManyToOne
    private User user_id;

}