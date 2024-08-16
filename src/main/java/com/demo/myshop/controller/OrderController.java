package com.demo.myshop.controller;

import com.demo.myshop.dto.CreateOrderDto;
import com.demo.myshop.dto.OrderDto;
import com.demo.myshop.model.Order;
import com.demo.myshop.security.UserDetailsImpl;
import com.demo.myshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        List<OrderDto> orders = orderService.getOrdersByUserAsDto(userId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelOrder(@RequestParam Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok("Order canceled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/return")
    public ResponseEntity<String> requestReturn(@RequestParam Long orderId) {
        try {
            orderService.requestReturn(orderId);
            return ResponseEntity.ok("Return request submitted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/return/complete")
    public ResponseEntity<String> completeReturn(@RequestParam Long orderId) {
        try {
            orderService.completeReturn(orderId);
            return ResponseEntity.ok("Return completed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestBody CreateOrderDto createOrderDto) {
        try {
            Long userId = userDetails.getUser().getId();
            Order order = orderService.createOrder(userId, createOrderDto.getItems());
            return ResponseEntity.ok("Order created successfully with ID: " + order.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}