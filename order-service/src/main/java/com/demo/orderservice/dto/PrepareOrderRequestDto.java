package com.demo.orderservice.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PrepareOrderRequestDto {
    // todo : 추후에 기존 OrderItemDto랑 둘 중에 하나만 남기기 / 여러개 있어서 헷갈림..!
    private Long productId;
    private Integer quantity;
    private Integer price;
}
