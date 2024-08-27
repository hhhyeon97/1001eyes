package com.demo.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PrepareOrderDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String userId;
    private List<PrepareOrderRequestDto> orderItems;
    private LocalDateTime createdAt;
}