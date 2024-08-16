package com.demo.myshop.service;

import com.demo.myshop.dto.OrderDto;
import com.demo.myshop.dto.OrderItemDto;
import com.demo.myshop.model.*;
import com.demo.myshop.repository.OrderRepository;
import com.demo.myshop.repository.OrderItemRepository;
import com.demo.myshop.repository.ProductRepository;
import com.demo.myshop.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<OrderDto> getOrdersByUserAsDto(Long userId) {
    List<Order> orders = orderRepository.findByUserId(userId);

    return orders.stream().map(order -> {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setOrderDate(order.getOrderDate());
        orderDto.setDeliveryDate(order.getDeliveryDate());
        orderDto.setTotalPrice(order.getTotalPrice());
        orderDto.setStatus(order.getStatus());

        // OrderItemDto 변환 및 설정
        List<OrderItemDto> items = order.getItems().stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getTitle());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setPrice(item.getPrice());
            return itemDto;
        }).collect(Collectors.toList());

        orderDto.setItems(items);
        return orderDto;
    }).collect(Collectors.toList());
}

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELED) {
            throw new RuntimeException("Order cannot be canceled");
        }

        order.updateStatus(OrderStatus.CANCELED);

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        orderRepository.save(order);
    }

    public void requestReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.DELIVERED || order.getReturnRequestDate() != null) {
            throw new RuntimeException("Return request not allowed");
        }

        LocalDateTime requestDate = LocalDateTime.now();
        if (requestDate.isAfter(order.getDeliveryDate().plusDays(1))) {
            throw new RuntimeException("Return period expired");
        }

        order.setReturnRequestDate(requestDate);
        order.updateStatus(OrderStatus.RETURN_REQUESTED);

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        orderRepository.save(order);
    }

    public void completeReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.RETURN_REQUESTED) {
            throw new RuntimeException("Order return request not found");
        }

        order.updateStatus(OrderStatus.RETURNED);
        orderRepository.save(order);
    }

    public Order createOrder(Long userId, List<OrderItemDto> orderItems) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 총 가격 계산 및 재고 확인
        int totalPrice = 0;

        for (OrderItemDto dto : orderItems) {
            // 제품 조회
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // 재고 확인
            if (product.getStock() < dto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product " + product.getId());
            }

            // 총 가격 업데이트
            totalPrice += product.getPrice() * dto.getQuantity();
        }

        // 재고가 충분한 경우에만 주문 객체 생성
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(totalPrice);

        // 주문 먼저 저장
        orderRepository.save(order);

        // OrderItem 생성 및 저장
        for (OrderItemDto dto : orderItems) {
            // 제품 조회 (이미 재고 확인은 했으므로 재조회 필요 없음)
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // 제품 재고 감소
            product.setStock(product.getStock() - dto.getQuantity());
            productRepository.save(product);

            // OrderItem 객체 생성 및 설정
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            item.setPrice(product.getPrice());
            item.setOrder(order);

            // OrderItem 저장
            orderItemRepository.save(item);
        }

        return order;
    }
}