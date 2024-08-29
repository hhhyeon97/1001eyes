package com.demo.orderservice.dto;

import com.demo.orderservice.model.Order;
import com.demo.orderservice.model.OrderItem;
import com.demo.orderservice.model.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class PrepareOrderDto implements Serializable {

    private Long orderId;
    private String userId;
    private List<PrepareOrderRequestDto> orderItems;
    private LocalDateTime createdAt;
    private OrderStatus status;  // 주문 상태
    private List<PaymentRequestDto> paymentItems;  // 결제 아이템 (결제 단계에서 추가)
    private LocalDateTime paymentAt;  // 결제 진입 시간 (이건 나중에 필요 없을 수도 일단 두기)

    // toEntity 메서드 -> PrepareOrderDto를 JPA 엔티티인 Order로 변환하는 역할
    public Order toEntity() {
        Order order = new Order();
        order.setId(this.orderId);
        order.setUserId(this.userId);
        order.setOrderDate(this.createdAt);

        // 주문 상태를 COMPLETED로 설정
        order.setStatus(OrderStatus.COMPLETED);

        // OrderItems를 OrderItem 엔티티로 변환
        Set<OrderItem> items = this.orderItems.stream()
                .map(itemDto -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(itemDto.getProductId());
                    orderItem.setQuantity(itemDto.getQuantity());
                    orderItem.setPrice(itemDto.getPrice()); // PrepareOrderRequestDto에서 가격 가져오기
                    orderItem.setOrder(order);
                    return orderItem;
                }).collect(Collectors.toSet());  // List 대신 Set으로 변환

        order.setItems(items);

        // 총 가격 계산
        int totalPrice = items.stream()
                .mapToInt(item -> item.getQuantity() * item.getPrice())  // item의 수량과 가격을 곱함
                .sum();

        order.setTotalPrice(totalPrice);

        return order;
    }

}