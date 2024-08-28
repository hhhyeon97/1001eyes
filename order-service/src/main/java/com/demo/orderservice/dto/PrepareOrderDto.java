package com.demo.orderservice.dto;

import com.demo.orderservice.model.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PrepareOrderDto implements Serializable {

    private Long orderId;
    private String userId;
    private List<PrepareOrderRequestDto> orderItems;
    private LocalDateTime createdAt;
    private OrderStatus status;  // 주문 상태
    private List<PaymentRequestDto> paymentItems;  // 결제 아이템 (결제 단계에서 추가)
    private LocalDateTime paymentAt;  // 결제 시간 = 실제 오더 생성 시간으로 봐도 될 듯 ( 오더 테이블에 있는 orderDate )

}