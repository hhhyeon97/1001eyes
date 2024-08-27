package com.demo.orderservice.model;

public enum OrderStatus {
    PENDING,           // 주문 대기 (주문 생성됨)
    PAYING,            // 결제 대기 (결제 진행 중)
    TIME_OUT,          // 타임아웃 (주문 또는 결제 이탈 시)
    COMPLETED,         // 결제 성공 (주문 최종 완료)
    // ========================================
    PREPARING,         // 배송 준비 중 (결제 완료 후)
    SHIPPED,           // 배송 중
    DELIVERED,         // 배송 완료
    CANCELED,          // 주문 취소됨
    RETURN_REQUESTED,  // 반품 요청됨
    RETURNED           // 반품 완료
}