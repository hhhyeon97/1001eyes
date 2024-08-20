package com.demo.orderservice.service;

import com.demo.orderservice.dto.OrderDto;
import com.demo.orderservice.model.Order;
import com.demo.orderservice.repository.OrderItemRepository;
import com.demo.orderservice.repository.OrderRepository;
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

    // 주문 취소 메서드
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문 정보가 없습니다."));

        // 상태가 '배송중' 이상인 경우 취소 불가
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("이미 배송 중이거나 배달 중이므로 주문을 취소할 수 없습니다.");
        }

        // 재고 복구
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        // 주문 상태를 '취소됨'으로 변경
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    // 반품 요청
    public void requestReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문 정보가 없습니다."));

        if (order.getStatus() != OrderStatus.DELIVERED || order.getReturnRequestDate() != null) {
            throw new RuntimeException("반품 요청 불가");
        }

        LocalDateTime requestDate = LocalDateTime.now();
        if (requestDate.isAfter(order.getDeliveryDate().plusMinutes(5))) {
            throw new RuntimeException("반품 기간이 만료되었습니다.");
        }

        order.setReturnRequestDate(requestDate);
        order.updateStatus(OrderStatus.RETURN_REQUESTED);

        // 재고 변경은 스케줄러에서 처리하므로 여기서는 제외
        orderRepository.save(order);
    }

    public Order createOrder(Long userId, List<OrderItemDto> orderItems) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 총 가격 계산 및 재고 확인
        int totalPrice = 0;

        for (OrderItemDto dto : orderItems) {
            // 제품 조회
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("해당 상품 없음"));

            // 재고 확인
            if (product.getStock() < dto.getQuantity()) {
                throw new RuntimeException("상품 재고 부족 : " + product.getId());
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
                    .orElseThrow(() -> new RuntimeException("해당 상품 없음"));

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