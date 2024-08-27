package com.demo.orderservice.controller;

import com.demo.orderservice.dto.OrderDto;
import com.demo.orderservice.dto.PaymentRequestDto;
import com.demo.orderservice.dto.PrepareOrderRequestDto;
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

    // 상품 상세 -> 주문 버튼 눌렀을 때
    // 레디스 조회해서 사려는 개수 차감
    // 상품 id, 상품 개수, 생성 시간 -> 객체로 만들어서 레디스에 저장
    // -> 주문마다 고유 번호 (중복 x) 주고 주문 객체의 키로 쓰기
    @PostMapping("/prepare")
    public ResponseEntity<?> prepareOrders(@RequestHeader("X-Auth-User-ID") String userId,
                                           @RequestBody List<PrepareOrderRequestDto> prepareOrderRequestDtoList) {
        try {
            Long order_key = orderService.prepareOrder(userId, prepareOrderRequestDtoList);
            return ResponseEntity.ok(order_key);
        }catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 주문 페이지 -> 결제하러 가기 버튼 눌렀을 때
    // 작성한 주문 정보를 레디스에 저장
    @PostMapping("/payment")
    public ResponseEntity<?> paymentOrders(@RequestHeader("X-Auth-User-ID") String userId,
                                           @RequestBody List<PaymentRequestDto> paymentRequestDtoList) {
        try{
            Long payment_key = orderService.preparePayment(userId, paymentRequestDtoList);
            return ResponseEntity.ok(payment_key);
        }catch(Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    /*// 주문 생성
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestHeader("X-Auth-User-ID") String userId,
                                              @RequestBody OrderDto orderDto) {
        try {
            Order order = orderService.createOrder(userId, orderDto.getItems());
            return ResponseEntity.ok("주문 완료 -> 주문번호 : " + order.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }*/

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

    // 상품 반품 요청
    @PostMapping("/return")
    public ResponseEntity<String> requestReturn(@RequestParam Long orderId) {
        try {
            orderService.requestReturn(orderId);
            return ResponseEntity.ok("반품 요청 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}