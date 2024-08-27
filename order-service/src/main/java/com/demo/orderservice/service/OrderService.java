package com.demo.orderservice.service;

import com.demo.orderservice.client.ProductServiceClient;
import com.demo.orderservice.dto.*;
import com.demo.orderservice.model.Order;
import com.demo.orderservice.model.OrderItem;
import com.demo.orderservice.model.OrderStatus;
import com.demo.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j(topic = "오더 서비스")
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
//    private final StringRedisTemplate redisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ORDER_KEY_SEQUENCE = "order:key:sequence";
    private static final String PAYMENT_KEY_SEQUENCE = "payment:key:sequence";



    public OrderService(OrderRepository orderRepository, ProductServiceClient productServiceClient, RedisTemplate<String, Object> redisTemplate) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
        this.redisTemplate = redisTemplate;
    }

    private static final String STOCK_RECOVERY_KEY_PREFIX = "stock_recovery:";

    // 주문 조회
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

    // todo : 리팩토링
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

    // 주문 취소 리팩토링 1 : 기존 - 주문 취소시 바로 재고 복구 -> 변경 - 스케줄러로 며칠 있다 복구되게 설정 (캔슬날짜 컬럼 추가)
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문 정보가 없습니다."));

        // 상태가 '배송중' 이상인 경우 취소 불가
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("이미 배송 중이거나 배달 중이므로 주문을 취소할 수 없습니다.");
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


    @Transactional
    public Long prepareOrder(String userId, List<PrepareOrderRequestDto> prepareOrderRequestDtoList) {
        // 1. 주문에 대한 고유한 키 생성 (Long)
        Long orderKey = redisTemplate.opsForValue().increment(ORDER_KEY_SEQUENCE);
        if (orderKey == null) {
            throw new IllegalStateException("주문 키 생성에 실패했습니다.");
        }
        // 2. 각 상품에 대해 재고 확인 및 차감
        for (PrepareOrderRequestDto requestDto : prepareOrderRequestDtoList) {
            Long productId = requestDto.getProductId();
            Integer quantityToOrder = requestDto.getQuantity();
            String stockKey = "stock:" + productId;

            // 레디스에서 재고 조회
            Integer currentStock = (Integer) redisTemplate.opsForValue().get(stockKey);

            // Redis에 재고 정보가 없으면 ProductService를 통해 재고 조회
            if (currentStock == null) {
                // ProductServiceClient를 사용하여 실제 DB에서 재고 조회
                ProductResponseDto productResponse = productServiceClient.getProductById(productId).getBody();
                if (productResponse == null) {
                    throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId);
                }
                currentStock = productResponse.getStock(); // 상품의 실제 재고

                // Redis에 재고 정보 캐싱 (초기값 설정)
                redisTemplate.opsForValue().set(stockKey, currentStock);
            }
            // 재고가 부족한 경우 예외 처리
            if (currentStock < quantityToOrder) {
                throw new IllegalArgumentException("상품 재고가 부족합니다: " + productId);
            }
            // 재고 차감 (Redis에서)
            redisTemplate.opsForValue().decrement(stockKey, quantityToOrder);
            // ProductServiceClient를 사용하여 실제 DB의 재고도 업데이트
            productServiceClient.updateProductStock(productId, (currentStock - quantityToOrder));
        }

        // 3. 주문 객체 생성 (레디스에 담을 임시 dto 객체)
        PrepareOrderDto prepareOrderDto = new PrepareOrderDto();
        prepareOrderDto.setUserId(userId);
        prepareOrderDto.setOrderId(orderKey);
        prepareOrderDto.setOrderItems(prepareOrderRequestDtoList);
        prepareOrderDto.setCreatedAt(LocalDateTime.now());

        // 4. Redis에 주문 객체 저장
        redisTemplate.opsForHash().put("orders", orderKey.toString(), prepareOrderDto);

        return orderKey; // Long 타입 주문 키 반환
    }

    @Transactional
    public Long preparePayment(String userId, List<PaymentRequestDto> paymentRequestDtoList) {
        // 1. 결제에 대한 고유한 키 생성 (Long)
        Long paymentKey = redisTemplate.opsForValue().increment(PAYMENT_KEY_SEQUENCE);
        if (paymentKey == null) {
            throw new IllegalStateException("결제 키 생성에 실패했습니다.");
        }
        // 2. 결제 객체 생성 (레디스에 담을 임시 dto 객체)
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setPaymentId(paymentKey);
        paymentDto.setUserId(userId);
        paymentDto.setPaymentItems(paymentRequestDtoList);
        paymentDto.setCreatedAt(LocalDateTime.now());
        // 3. 레디스에 결제 객체 저장
        redisTemplate.opsForHash().put("payments", paymentKey.toString(), paymentDto);

        return paymentKey;  // Long 타입 결제 키 반환
    }

}