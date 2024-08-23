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

    // todo : 수정한 흐름 정리하고 기존 코드 제거하기
    // 게이트웨이에서 헤더에 넣어줬잖아 -/> 헤더에서 뽑아오는걸로 변경
    // 기존 코드
//    @GetMapping
//    public ResponseEntity<List<OrderDto>> getOrders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
//        Long userId = userDetails.getUser().getId();
//        List<OrderDto> orders = orderService.getOrdersByUserAsDto(userId);
//        return ResponseEntity.ok(orders);
//    }

//    // 수정 코드 - 주문 조회
//    @GetMapping
//    public ResponseEntity<List<OrderDto>> getOrders(@RequestHeader("X-Auth-User-ID") String userId) {
//        List<OrderDto> orders = orderService.getOrdersByUserAsDto(userId);
//        return ResponseEntity.ok(orders);
//    }

//    @PostMapping("/cancel")
//    public ResponseEntity<String> cancelOrder(@RequestParam Long orderId) {
//        try{
//            orderService.cancelOrder(orderId);
//            return ResponseEntity.ok("주문 취소 완료");
//        }catch(Exception e){
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    //    @PostMapping("/cancel")
//    public ResponseEntity<String> cancelOrder(@RequestParam Long orderId) {
//        try {
//            orderService.cancelOrder(orderId);
//            return ResponseEntity.ok("주문 취소 완료");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PostMapping("/return")
//    public ResponseEntity<String> requestReturn(@RequestParam Long orderId) {
//        try {
//            orderService.requestReturn(orderId);
//            return ResponseEntity.ok("반품 요청 완료");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//     기존 코드
//    @PostMapping
//    public ResponseEntity<String> createOrder(@AuthenticationPrincipal UserDetailsImpl userDetails,
//                                              @RequestBody CreateOrderDto createOrderDto) {
//        try {
//            Long userId = userDetails.getUser().getId();
//            Order order = orderService.createOrder(userId, createOrderDto.getItems());
//            return ResponseEntity.ok("주문 완료 -> 주문번호 : " + order.getId());
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//    // 주문 생성
//    @PostMapping
//    public ResponseEntity<OrderDto> createOrder(@RequestHeader("X-Auth-User-ID") String userId,
//                                                @RequestBody OrderDto orderDto) {
//        try {
//            Order order = orderService.createOrder(userId, orderDto.getItems());
//            // 생성된 Order를 OrderDto로 변환하여 반환
//            OrderDto createdOrderDto = new OrderDto();
//            createdOrderDto.setId(order.getId());
//            createdOrderDto.setOrderDate(order.getOrderDate());
//            createdOrderDto.setTotalPrice(order.getTotalPrice());
//            createdOrderDto.setStatus(order.getStatus());
//            createdOrderDto.setItems(orderDto.getItems()); // OrderItemDto 리스트 설정
//            return ResponseEntity.ok(createdOrderDto);
//        } catch (Exception e) {
////            e.printStackTrace();
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
}