package com.demo.orderservice.dto;

import com.demo.orderservice.model.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDto {
    private Long id;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private Integer totalPrice;
    private OrderStatus status;
    private List<OrderItemDto> items;
}