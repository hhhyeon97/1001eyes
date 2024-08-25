package com.demo.orderservice.model;

public enum OrderStatus {
    PENDING, // 주문 대기

    // todo : 추가 상태
    PAYING, // 결제 대기
    COMPLETED,  // 결제 성공 = 주문 최종 완료
    PREPARING, // 배송 준비중

    SHIPPED, // 배송중
    DELIVERED, // 배송 완료
    CANCELED, // 취소됨
    RETURN_REQUESTED, // 반품 요청됨
    RETURNED // 반품 완료
}