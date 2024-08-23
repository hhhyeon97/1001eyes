package com.demo.orderservice.controller;

import com.demo.orderservice.dto.OrderDto;
import com.demo.orderservice.model.Order;
import com.demo.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    // 주문 조회
    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders(@RequestHeader("X-Auth-User-ID") String userId) {
        List<OrderDto> orders = orderService.getOrdersByUserAsDto(userId);
        return ResponseEntity.ok(orders);
    }

    // 주문 생성
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestHeader("X-Auth-User-ID") String userId,
                                              @RequestBody OrderDto orderDto) {
        try {
            Order order = orderService.createOrder(userId, orderDto.getItems());
            return ResponseEntity.ok("주문 완료 -> 주문번호 : " + order.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 주문 취소
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelOrder(@RequestParam Long orderId) {
        try{
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok("주문 취소 완료");
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    @PostMapping("/return")
//    public ResponseEntity<String> requestReturn(@RequestParam Long orderId) {
//        try {
//            orderService.requestReturn(orderId);
//            return ResponseEntity.ok("반품 요청 완료");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}