package com.demo.orderservice.service;

import com.demo.orderservice.client.ProductServiceClient;
import com.demo.orderservice.dto.*;
import com.demo.orderservice.model.Order;
import com.demo.orderservice.model.OrderItem;
import com.demo.orderservice.model.OrderStatus;
import com.demo.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
    private final RedissonClient redissonClient;



    public OrderService(OrderRepository orderRepository, ProductServiceClient productServiceClient, RedisTemplate<String, Object> redisTemplate
    , ObjectMapper objectMapper, RedissonClient redissonClient) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.redissonClient = redissonClient;
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

  /* // 주문 진입 ( 실제 db 반영 x -> 레디스에 저장 )
    @Transactional(readOnly = false)
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
                log.info("db 조회");
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
        prepareOrderDto.setOrderItems(prepareOrderRequestDtoList);
        prepareOrderDto.setCreatedAt(LocalDateTime.now());
        prepareOrderDto.setStatus(OrderStatus.PENDING);

        // 4. Redis에 주문 객체 저장
        redisTemplate.opsForHash().put("orders", orderKey.toString(), prepareOrderDto);

        // ++ 결제에 대한 TTL 설정 (10분 -> 테스트 : 3분)
        redisTemplate.expire("orders:" + orderKey, 3, TimeUnit.MINUTES);

        return orderKey; // Long 타입 주문 키 반환
    }
*/
/*
    // 주문 진입 ( 실제 db 반영 x -> 레디스에 저장 )
    @Transactional(readOnly = false)
    public Long prepareOrder(String userId, List<PrepareOrderRequestDto> prepareOrderRequestDtoList) {
        // 1. 주문에 대한 고유한 키 생성 (Long)
        Long orderKey = redisTemplate.opsForValue().increment(ORDER_KEY_SEQUENCE);
        if (orderKey == null) {
            throw new IllegalStateException("주문 키 생성에 실패했습니다.");
        }

        // ++ 주문과 사용자 간의 매핑 저장
        String userOrderKey = "user_orders:" + userId;
        redisTemplate.opsForValue().set(userOrderKey, orderKey.toString());

        try {
            // 2. 각 상품에 대해 재고 확인 및 차감
            for (PrepareOrderRequestDto requestDto : prepareOrderRequestDtoList) {
                Long productId = requestDto.getProductId();
                Integer quantityToOrder = requestDto.getQuantity();
                String stockKey = "stock:" + productId;

                // 레디스에서 재고 조회
                Integer currentStock = (Integer) redisTemplate.opsForValue().get(stockKey);

                // Redis에 재고 정보가 없으면 ProductService를 통해 재고 조회
                if (currentStock == null) {
                    log.info("db 조회");
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
            prepareOrderDto.setOrderItems(prepareOrderRequestDtoList);
            prepareOrderDto.setCreatedAt(LocalDateTime.now());
            prepareOrderDto.setStatus(OrderStatus.PENDING);

            // 4. Redis에 주문 객체 저장
            redisTemplate.opsForHash().put("orders", orderKey.toString(), prepareOrderDto);

            // ++ 결제에 대한 TTL 설정 (10분 -> 테스트 : 3분)
            redisTemplate.expire("orders:" + orderKey, 3, TimeUnit.MINUTES);

            return orderKey; // Long 타입 주문 키 반환

        } catch (Exception e) {
            // 예외 발생 시 임시오더키 삭제
            redisTemplate.delete(userOrderKey);

            // 로그 남기기
            log.error("주문 준비 중 오류 발생: " + e.getMessage(), e);

            // 예외 재던지기
            throw e;

        }
    }*/

    // 주문진입
    @Transactional(readOnly = false)
    public Long prepareOrder(String userId, List<PrepareOrderRequestDto> prepareOrderRequestDtoList) {
        // 1. 주문에 대한 고유한 키 생성 (Long)
        Long orderKey = redisTemplate.opsForValue().increment(ORDER_KEY_SEQUENCE);
        if (orderKey == null) {
            throw new IllegalStateException("주문 키 생성에 실패했습니다.");
        }
        // ++ 주문과 사용자 간의 매핑 저장
        String userOrderKey = "user_orders:" + userId;
        redisTemplate.opsForValue().set(userOrderKey, orderKey.toString());

        try {
            // 2. 각 상품에 대해 재고 확인 및 차감
            for (PrepareOrderRequestDto requestDto : prepareOrderRequestDtoList) {
                Long productId = requestDto.getProductId();
                Integer quantityToOrder = requestDto.getQuantity();
                String stockKey = "stock:" + productId;

                // 상품별로 개별 락 생성 및 적용
                RLock productLock = redissonClient.getLock("stock_lock:" + productId);
                try {
                    if (productLock.tryLock(3, 6, TimeUnit.SECONDS)) {
                        try {
                            // 레디스에서 재고 조회
                            Integer currentStock = (Integer) redisTemplate.opsForValue().get(stockKey);

                            // Redis에 재고 정보가 없으면 ProductService를 통해 재고 조회
                            if (currentStock == null) {
                                log.info("db 조회");
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
                        } finally {
                            productLock.unlock();  // 락 해제
                        }
                    } else {
                        throw new RuntimeException("재고 차감을 위한 락을 획득하지 못했습니다.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("재고 차감 중 인터럽트 발생", e);
                }
            }

            // 3. 주문 객체 생성 (레디스에 담을 임시 dto 객체)
            PrepareOrderDto prepareOrderDto = new PrepareOrderDto();
            prepareOrderDto.setUserId(userId);
            prepareOrderDto.setOrderItems(prepareOrderRequestDtoList);
            prepareOrderDto.setCreatedAt(LocalDateTime.now());
            prepareOrderDto.setStatus(OrderStatus.PENDING);

            // 4. Redis에 주문 객체 저장
            redisTemplate.opsForHash().put("orders", orderKey.toString(), prepareOrderDto);

            // ++ 결제에 대한 TTL 설정 (10분 -> 테스트 : 3분)
            redisTemplate.expire("orders:" + orderKey, 3, TimeUnit.MINUTES);

            return orderKey; // Long 타입 주문 키 반환
        } catch (Exception e) {
            log.error("주문 준비 중 예외 발생", e);
            // 실패한 경우, 사용자와 매핑된 임시오더키 삭제
            redisTemplate.delete(userOrderKey);
            throw e;
        }
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
//        orderDto.setPaymentAt(LocalDateTime.now());

        // 4. Redis에 업데이트된 주문 객체 저장
        redisTemplate.opsForHash().put("orders", orderKey.toString(), orderDto);

        // ++ 주문에 대한 TTL 설정 (10분 -> 테스트 : 3분)
        redisTemplate.expire("orders:" + orderKey, 3, TimeUnit.MINUTES);

        return orderKey;  // Long 타입 결제 키 반환
    }

    /**
     * 결제 완료시
     * @param userId
     * @return
     */
//    @Transactional
//    public ResponseEntity<?> completePayment(String userId) {
//        // 1. 주문 키와 주문 객체 조회
//        String userOrderKey = "user_orders:" + userId;
//        String orderKeyStr = (String) redisTemplate.opsForValue().get(userOrderKey);
//
//        if (orderKeyStr == null) {
//            return ResponseEntity.badRequest().body("해당 사용자의 주문이 없습니다.");
//        }
//
//        Long orderKey = Long.parseLong(orderKeyStr);
//        Object orderObject = redisTemplate.opsForHash().get("orders", orderKey.toString());
//        if (orderObject == null) {
//            return ResponseEntity.badRequest().body("주문을 찾을 수 없습니다.");
//        }
//
//        PrepareOrderDto orderDto = objectMapper.convertValue(orderObject, PrepareOrderDto.class);
//
//        try {
//            // 2. 주문 객체를 실제 주문 테이블에 저장
//            Order order = orderDto.toEntity();
//            order.setStatus(OrderStatus.COMPLETED);  // 주문 상태를 COMPLETED로 설정
//            orderRepository.save(order);
//
//            // 3. 저장된 주문 정보 기반으로 재고 차감 수행
//            Set<OrderItem> orderItems = order.getItems();
//            for (OrderItem item : orderItems) {
//                Long productId = item.getProductId();
//                int quantityOrdered = item.getQuantity();
//
//                // 상품 서비스에 재고 차감 요청
//                productServiceClient.checkAndDeductStock(productId, quantityOrdered);
//                // todo : 지금은 받아온 결과 뭐 쓰는건 없는데 추후에 에러메세지를 반환 받을 때
//                // 추가적인 처리하면 좋겠다 !
//            }
//
//            // 4. 주문 성공 후 Redis에서 해당 주문 데이터 삭제
//            redisTemplate.opsForHash().delete("orders", orderKey.toString());
//            redisTemplate.delete(userOrderKey);
//
//            return ResponseEntity.ok("결제가 완료되었습니다.");
//        } catch (Exception e) {
//            // 예외 발생 시 롤백 자동 수행 (트랜잭션 관리에 의해)
//            log.error("결제 처리 중 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 처리 중 오류가 발생했습니다.");
//        }
//    }

    /**
     * 결제 완료
     */
    public ResponseEntity<?> completePayment(String userId) {
        try {
            log.debug("시작: completePayment for user {}", userId);
            String userOrderKey = "user_orders:" + userId;
            String orderKeyStr = (String) redisTemplate.opsForValue().get(userOrderKey);
            if (orderKeyStr == null) {
                throw new IllegalArgumentException("해당 사용자의 주문이 없습니다.");
            }
            Long orderKey = Long.parseLong(orderKeyStr);

            PrepareOrderDto orderDto = getOrderFromRedis(orderKey);
            log.debug("Redis에서 주문 정보 조회 완료: {}", orderDto);
            Order savedOrder = saveOrder(orderDto);
            log.debug("주문 저장 완료: {}, {}", savedOrder.getId(), orderKey);
            deductStock(savedOrder);
            log.debug("재고 차감 완료");
            cleanupRedisData(userId, orderKey);
            log.debug("Redis 데이터 정리 완료");
            return ResponseEntity.ok("결제가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 처리 중 오류가 발생했습니다.");
        } finally {
            log.debug("종료: completePayment for user {}", userId);
        }
    }

    private PrepareOrderDto getOrderFromRedis(Long orderKey) {

        Object orderObject = redisTemplate.opsForHash().get("orders", orderKey.toString());
        if (orderObject == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다.");
        }
        return objectMapper.convertValue(orderObject, PrepareOrderDto.class);
    }

    @Transactional
    public Order saveOrder(PrepareOrderDto orderDto) {
        try {
            Order order = orderDto.toEntity();
            order.setStatus(OrderStatus.COMPLETED);
            return orderRepository.save(order);
        } catch (DataAccessException e) {
            log.error("주문 저장 중 데이터베이스 오류 발생", e);
            throw new RuntimeException("주문을 저장하는 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("주문 저장 중 예상치 못한 오류 발생", e);
            throw new RuntimeException("주문을 저장하는 중 예상치 못한 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public void deductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Long productId = item.getProductId();
            int quantityOrdered = item.getQuantity();
            try {
                productServiceClient.checkAndDeductStock(productId, quantityOrdered);
            } catch (Exception e) {
                log.error("재고 차감 중 오류 발생. 상품 ID: {}", productId, e);
                throw new RuntimeException("재고 차감 중 오류가 발생했습니다. 상품 ID: " + productId, e);
            }
        }
    }

    private void cleanupRedisData(String userId, Long orderKey) {
        try {
            String userOrderKey = "user_orders:" + userId;
            redisTemplate.opsForHash().delete("orders", orderKey.toString());
            redisTemplate.delete(userOrderKey);
        } catch (Exception e) {
            log.error("Redis 데이터 정리 중 오류 발생", e);
            // Redis 정리 실패는 크리티컬한 오류가 아니므로 예외를 던지지 않고 로그만 남깁니다.
        }
    }


  /*  @Transactional
    public Long prepareOrder(String userId, List<PrepareOrderRequestDto> prepareOrderRequestDtoList) {
        // 1. 주문에 대한 고유한 키 생성
        Long orderKey = System.currentTimeMillis(); // 간단한 방법으로 키 생성

        // 2. 주문 객체 생성
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // 3. 각 상품에 대해 재고 확인 및 차감
        for (PrepareOrderRequestDto requestDto : prepareOrderRequestDtoList) {
            Long productId = requestDto.getProductId();
            Integer quantityToOrder = requestDto.getQuantity();

            // DB에서 재고 조회 (캐싱 없이 직접 조회)
            Integer currentStock = productServiceClient.getProductByInternalId(productId).getBody();

            // 재고가 부족한 경우 예외 처리
            if (currentStock < quantityToOrder) {
                throw new IllegalArgumentException("상품 재고가 부족합니다: " + productId);
            }

            // DB에서 재고 차감
            productServiceClient.checkAndDeductStock(productId, quantityToOrder);

            // 주문 아이템 추가
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(productId);
            orderItem.setQuantity(quantityToOrder);
            order.getItems().add(orderItem);
        }
        // 4. DB에 주문 객체 저장
        orderRepository.save(order);

        return orderKey;
    }

    @Transactional
    public Long preparePayment(String userId, List<PaymentRequestDto> paymentRequestDtoList) {
        // 1. DB에서 orderKey 찾기
        Order order = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 주문이 없습니다."));

        Long orderKey = order.getId();

        // 2. 주문 객체 업데이트
//        order.setItems(paymentRequestDtoList);
        order.setStatus(OrderStatus.PAYING);

        // 3. DB에 업데이트된 주문 객체 저장
        orderRepository.save(order);

        return orderKey;  // Long 타입 결제 키 반환
    }

    @Transactional
    public ResponseEntity<?> completePayment(String userId) {
        try {
            log.debug("시작: completePayment for user {}", userId);

            // 1. DB에서 주문 조회
            Order order = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PAYING)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 주문이 없습니다."));

            // 2. 주문 상태를 완료로 변경
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            // 3. 재고 차감
            deductStock(order);
            log.debug("재고 차감 완료");

            return ResponseEntity.ok("결제가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 처리 중 오류가 발생했습니다.");
        } finally {
            log.debug("종료: completePayment for user {}", userId);
        }
    }


    @Transactional
    public void deductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Long productId = item.getProductId();
            int quantityOrdered = item.getQuantity();
            try {
                productServiceClient.checkAndDeductStock(productId, quantityOrdered);
            } catch (Exception e) {
                log.error("재고 차감 중 오류 발생. 상품 ID: {}", productId, e);
                throw new RuntimeException("재고 차감 중 오류가 발생했습니다. 상품 ID: " + productId, e);
            }
        }
    }*/

}