package com.demo.orderservice.service;

import com.demo.orderservice.client.ProductServiceClient;
import com.demo.orderservice.dto.*;
import com.demo.orderservice.model.Order;
import com.demo.orderservice.model.OrderItem;
import com.demo.orderservice.model.OrderStatus;
import com.demo.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j(topic = "오더 서비스")
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private ObjectMapper objectMapper;
    private static final String ORDER_KEY_SEQUENCE = "order:key:sequence";


    public OrderService(OrderRepository orderRepository, ProductServiceClient productServiceClient, RedisTemplate<String, Object> redisTemplate
    , ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

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
   /* @Transactional // 트랜잭션 관리 적용
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
    }*/

    // 주문 취소 (결제 완료 후 - 배송준비전까지 가능)
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문 정보가 없습니다."));

        // 상태가 '배송 준비중' 이상인 경우 취소 불가
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("이미 배송 준비중이거나 배달 중이므로 주문을 취소할 수 없습니다.");
        }
        // 주문 상태를 '취소됨'으로 변경
        order.setStatus(OrderStatus.CANCELED);
        order.setCancelDate(LocalDateTime.now());
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

    // 주문 진입 ( 실제 db 반영 x -> 레디스에 저장 )
    @Transactional
    public Long prepareOrder(String userId, List<PrepareOrderRequestDto> prepareOrderRequestDtoList) {
        // 1. 주문에 대한 고유한 키 생성 (Long)
        Long orderKey = redisTemplate.opsForValue().increment(ORDER_KEY_SEQUENCE);
        if (orderKey == null) {
            throw new IllegalStateException("주문 키 생성에 실패했습니다.");
        }
        // ++ 주문과 사용자 간의 매핑 저장
        String userOrderKey = "user_orders:" + userId;
        redisTemplate.opsForValue().set(userOrderKey, orderKey.toString());

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
                Integer dbStock = productServiceClient.getProductByInternalId(productId).getBody();

                currentStock = dbStock; // 상품의 실제 재고

                // Redis에 재고 정보 캐싱 (초기값 설정)
                redisTemplate.opsForValue().set(stockKey, currentStock);
            }
            // 재고가 부족한 경우 예외 처리
            if (currentStock < quantityToOrder) {
                throw new IllegalArgumentException("상품 재고가 부족합니다: " + productId);
            }
            // Redis에서만 재고 차감
            redisTemplate.opsForValue().decrement(stockKey, quantityToOrder);
        }
        // 3. 주문 객체 생성 (레디스에 담을 임시 dto 객체)
        PrepareOrderDto prepareOrderDto = new PrepareOrderDto();
        prepareOrderDto.setUserId(userId);
        prepareOrderDto.setOrderId(orderKey);
        prepareOrderDto.setOrderItems(prepareOrderRequestDtoList);
        prepareOrderDto.setCreatedAt(LocalDateTime.now());
        prepareOrderDto.setStatus(OrderStatus.PENDING);

        // 4. Redis에 주문 객체 저장
        redisTemplate.opsForHash().put("orders", orderKey.toString(), prepareOrderDto);

        // ++ 결제에 대한 TTL 설정 (10분 -> 테스트 : 3분)
        redisTemplate.expire("orders:" + orderKey, 3, TimeUnit.MINUTES);

        return orderKey; // Long 타입 주문 키 반환
    }

    // 결제 진입 ( 실제 db 반영 x -> 레디스에 저장 )
    @Transactional
    public Long preparePayment(String userId, List<PaymentRequestDto> paymentRequestDtoList) {
        // 1. userId를 사용하여 orderKey를 찾기
        String userOrderKey = "user_orders:" + userId;
        String orderKeyStr = (String) redisTemplate.opsForValue().get(userOrderKey);

        if (orderKeyStr == null) {
            throw new IllegalArgumentException("해당 사용자의 주문이 없습니다: " + userId);
        }

        Long orderKey = Long.parseLong(orderKeyStr);

        // 2. Redis에서 주문 객체 조회
        Object orderObject = redisTemplate.opsForHash().get("orders", orderKey.toString());
        if (orderObject == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderKey);
        }

        // ++ LinkedHashMap을 PrepareOrderDto로 변환
        PrepareOrderDto orderDto = objectMapper.convertValue(orderObject, PrepareOrderDto.class);

        // 3. 결제 정보 추가 및 결제 상태로 전환
        orderDto.setPaymentItems(paymentRequestDtoList);
        orderDto.setStatus(OrderStatus.PAYING);
        orderDto.setPaymentAt(LocalDateTime.now());

        // 4. Redis에 업데이트된 주문 객체 저장
        redisTemplate.opsForHash().put("orders", orderKey.toString(), orderDto);

        // ++ 주문에 대한 TTL 설정 (10분 -> 테스트 : 3분)
        redisTemplate.expire("orders:" + orderKey, 3, TimeUnit.MINUTES);

        return orderKey;  // Long 타입 결제 키 반환
    }

    // 결제 완료시
    @Transactional
    public ResponseEntity<?> completePayment(String userId) {
        // 1. 주문 키와 주문 객체 조회
        String userOrderKey = "user_orders:" + userId;
        String orderKeyStr = (String) redisTemplate.opsForValue().get(userOrderKey);

        if (orderKeyStr == null) {
            return ResponseEntity.badRequest().body("해당 사용자의 주문이 없습니다.");
        }

        Long orderKey = Long.parseLong(orderKeyStr);
        Object orderObject = redisTemplate.opsForHash().get("orders", orderKey.toString());
        if (orderObject == null) {
            return ResponseEntity.badRequest().body("주문을 찾을 수 없습니다.");
        }

        PrepareOrderDto orderDto = objectMapper.convertValue(orderObject, PrepareOrderDto.class);

        try {
            // 2. 주문 객체를 실제 주문 테이블에 저장
            Order order = orderDto.toEntity();
            order.setStatus(OrderStatus.COMPLETED);  // 주문 상태를 COMPLETED로 설정
            orderRepository.save(order);

            // 3. 저장된 주문 정보 기반으로 재고 차감 수행
            Set<OrderItem> orderItems = order.getItems();
            for (OrderItem item : orderItems) {
                Long productId = item.getProductId();
                int quantityOrdered = item.getQuantity();

                // 상품 서비스에서 실제 db 재고 가져오기 !
                Integer currentStock = productServiceClient.getProductByInternalId(productId).getBody();

                // 재고 확인 및 차감
                int updatedStock = currentStock - quantityOrdered;
                if (updatedStock < 0) {
                    throw new RuntimeException("상품 재고 부족: " + productId);
                }
                // todo : 재고 차감 요청 -> 동시성 문제 -> 재고를 확인하는 시점이랑 차감하는 시점 시간 차이날 가능성
                // -> 재고 확인 하고 차감하는 걸 오더에서 x - > 상품 서비스에서 처리

                // 상품 서비스에 차감한 재고 정보 넘겨서 db 상품 재고 업데이트 !
                productServiceClient.updateProductStock(productId, updatedStock);
            }
            // 4. 주문 성공 후 Redis에서 해당 주문 데이터 삭제
            redisTemplate.opsForHash().delete("orders", orderKey.toString());
            redisTemplate.delete(userOrderKey);

            return ResponseEntity.ok("결제가 완료되었습니다.");
        } catch (Exception e) {
            // 예외 발생 시 롤백 자동 수행 (트랜잭션 관리에 의해)
            log.error("결제 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 처리 중 오류가 발생했습니다.");
        }
    }

}