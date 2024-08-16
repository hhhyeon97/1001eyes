package com.demo.myshop.model;

public enum OrderStatus {
    PENDING, // 주문 대기
    SHIPPED, // 배송중
    DELIVERED, // 배송 완료
    CANCELED, // 취소됨
    RETURN_REQUESTED, // 반품 요청됨
    RETURNED // 반품 완료
}