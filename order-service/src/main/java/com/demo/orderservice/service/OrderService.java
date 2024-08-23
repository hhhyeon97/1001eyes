package com.demo.orderservice.service;

import com.demo.orderservice.client.ProductServiceClient;
import com.demo.orderservice.dto.OrderDto;
import com.demo.orderservice.dto.OrderItemDto;
import com.demo.orderservice.dto.ProductResponseDto;
import com.demo.orderservice.model.Order;
import com.demo.orderservice.model.OrderItem;
import com.demo.orderservice.model.OrderStatus;
import com.demo.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j(topic = "오더 서비스")
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    public OrderService(OrderRepository orderRepository, ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
    }

    public List<OrderDto> getOrdersByUserAsDto(String userId) {
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
                itemDto.setProductId(item.getProductId());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setPrice(item.getPrice());

                // Feign Client를 통해 상품 정보 가져오기
                ResponseEntity<ProductResponseDto> responseEntity = productServiceClient.getProductById(item.getProductId());

                // 상품 정보가 정상적으로 반환된 경우에만 정보 설정
                if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                    ProductResponseDto productDto = responseEntity.getBody();
                    itemDto.setProductName(productDto.getTitle());
                    itemDto.setPrice(productDto.getPrice()); // 상품 가격 업데이트
                } else {
                    itemDto.setProductName("상품 조회 실패");
                }
                return itemDto;
            }).collect(Collectors.toList());

            orderDto.setItems(items);
            return orderDto;
        }).collect(Collectors.toList());

    }


    @Transactional // 트랜잭션 관리 적용
    public Order createOrder(String userId, List<OrderItemDto> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("주문 항목이 비어있습니다.");
        }

        int totalPrice = 0;
        List<ProductResponseDto> productsToUpdate = new ArrayList<>();

        try {
            // 모든 상품의 재고 확인
            for (OrderItemDto dto : orderItems) {
                ResponseEntity<ProductResponseDto> responseEntity = productServiceClient.getProductById(dto.getProductId());

                if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("상품 정보를 조회할 수 없습니다: " + dto.getProductId());
                }

                ProductResponseDto product = responseEntity.getBody();
                if (product == null) {
                    throw new RuntimeException("해당 상품을 찾을 수 없습니다: " + dto.getProductId());
                }

                // 재고 확인
                if (product.getStock() < dto.getQuantity()) {
                    throw new RuntimeException("상품 재고 부족: " + product.getId());
                }

                // 총 가격 계산
                totalPrice += product.getPrice() * dto.getQuantity();
                productsToUpdate.add(product);
            }

            // 모든 재고 확인이 끝난 후 재고 차감
            for (OrderItemDto dto : orderItems) {
                ProductResponseDto product = productsToUpdate.stream()
                        .filter(p -> p.getId().equals(dto.getProductId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("상품 정보를 찾을 수 없습니다: " + dto.getProductId()));

                int updatedStock = product.getStock() - dto.getQuantity();
                productServiceClient.updateProductStock(product.getId(), updatedStock);
            }

            // 주문 객체 생성 및 설정
            Order order = new Order();
            order.setUserId(userId);
            order.setStatus(OrderStatus.PENDING);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalPrice(totalPrice);

            // 주문 항목 추가
            for (OrderItemDto dto : orderItems) {
                ProductResponseDto product = productsToUpdate.stream()
                        .filter(p -> p.getId().equals(dto.getProductId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("상품 정보를 찾을 수 없습니다: " + dto.getProductId()));

                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(dto.getProductId());
                orderItem.setQuantity(dto.getQuantity());
                orderItem.setPrice(product.getPrice());
                orderItem.setOrder(order);

                order.getItems().add(orderItem);
            }

            // 주문 저장
            orderRepository.save(order);
            return order;

        } catch (Exception e) {
            // 예외 발생 시 롤백 자동 수행 (트랜잭션 관리에 의해)
            log.error("주문 생성 중 오류 발생", e);
            throw e;
        }
    }


//    // 주문 취소
//    public void cancelOrder(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("해당하는 주문 정보가 없습니다."));
//
//        // 상태가 '배송중' 이상인 경우 취소 불가
//        if (order.getStatus() != OrderStatus.PENDING) {
//            throw new RuntimeException("이미 배송 중이거나 배달 중이므로 주문을 취소할 수 없습니다.");
//        }
//
//        // 재고 복구
//        for (OrderItem item : order.getItems()) {
//            ProductResponseDto productDto = productClient.getProductById(item.getProductId()).getMessage();
//
//            if (productDto != null) {
//                productDto.setStock(productDto.getStock() + item.getQuantity());
//                productClient.updateProductStock(productDto.getId(), productDto);
//            } else {
//                throw new RuntimeException("상품 정보를 가져올 수 없습니다.");
//            }
//        }
//
//        // 주문 상태를 '취소됨'으로 변경
//        order.setStatus(OrderStatus.CANCELED);
//        orderRepository.save(order);
//    }


    /*

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
        }*/

}