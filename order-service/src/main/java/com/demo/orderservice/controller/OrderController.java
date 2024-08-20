package com.demo.orderservice.controller;

import com.demo.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // test
    @GetMapping("/aaa/{text}")
    public String test(@PathVariable String text) {
        System.out.println("여기는 오더 컨트롤러 !!");
        return orderService.test(text);
    }

   /* @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        List<OrderDto> orders = orderService.getOrdersByUserAsDto(userId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelOrder(@RequestParam Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok("주문 취소 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/return")
    public ResponseEntity<String> requestReturn(@RequestParam Long orderId) {
        try {
            orderService.requestReturn(orderId);
            return ResponseEntity.ok("반품 요청 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestBody CreateOrderDto createOrderDto) {
        try {
            Long userId = userDetails.getUser().getId();
            Order order = orderService.createOrder(userId, createOrderDto.getItems());
            return ResponseEntity.ok("주문 완료 -> 주문번호 : " + order.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }*/
}