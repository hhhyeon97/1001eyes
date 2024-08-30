//package com.demo.orderservice.service;
//
//import com.demo.orderservice.client.ProductServiceClient;
//import com.demo.orderservice.dto.ProductResponseDto;
//import com.demo.orderservice.model.Order;
//import com.demo.orderservice.model.OrderItem;
//import com.demo.orderservice.model.OrderStatus;
//import com.demo.orderservice.repository.OrderRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Component
//public class OrderStatusScheduler {
//
//    private final OrderRepository orderRepository;
//    private final ProductServiceClient productServiceClient;
//
//    public OrderStatusScheduler(OrderRepository orderRepository, ProductServiceClient productServiceClient) {
//        this.orderRepository = orderRepository;
//        this.productServiceClient = productServiceClient;
//    }
//
//    // todo : 리팩토링
//    // 매일 자정에 주문 상태 업데이트 작업 실행
//    // @Scheduled(cron = "0 0 0 * * ?")
//    @Transactional  // 더티체킹을 위해 트랜잭션 추가
//    @Scheduled(cron = "0 * * * * ?")  // test : 매 분마다 실행
//    public void updateOrderStatus() {
//        LocalDateTime now = LocalDateTime.now();
//        log.info("스케줄러 시작 시간 : {}", now);
//
//        // 'PENDING' 상태의 주문 목록 처리
//        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
//        for (Order order : pendingOrders) {
//            if (order.getOrderDate().plusMinutes(1).isBefore(now)) {
//                if (order.getStatus() != OrderStatus.SHIPPED) {
//                    order.setStatus(OrderStatus.SHIPPED);
//                    orderRepository.save(order);
//                    log.info("주문 ID {}의 상태가 SHIPPED로 변경됨", order.getId());
//                }
//            }
//        }
//
//        // 'SHIPPED' 상태의 주문 목록 처리
//        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);
//        for (Order order : shippedOrders) {
//            if (order.getOrderDate().plusMinutes(3).isBefore(now)) {
//                if (order.getStatus() != OrderStatus.DELIVERED) {
//                    order.setStatus(OrderStatus.DELIVERED);
//                    order.setDeliveryDate(now);
//                    orderRepository.save(order);
//                    log.info("주문 ID {}의 상태가 DELIVERED로 변경 + 배송 완료 날짜가 {}로 설정됨", order.getId(), now);
//                }
//            }
//        }
//
//       /* // 'CANCELED' 상태의 주문 목록 처리 (재고 복구)
//        List<Order> canceledOrders = orderRepository.findByStatus(OrderStatus.CANCELED);
//        for (Order order : canceledOrders) {
//            if (order.getCancelDate().plusMinutes(1).isBefore(now)) {  // test : 1분 후 재고 복구
//                try {
//                    for (OrderItem item : order.getItems()) {
//                        ResponseEntity<ProductResponseDto> responseEntity = productServiceClient.getProductById(item.getProductId());
//                        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
//                            throw new RuntimeException("상품 정보를 가져올 수 없습니다: " + item.getProductId());
//                        }
//
//                        ProductResponseDto productDto = responseEntity.getBody();
//                        if (productDto == null) {
//                            throw new RuntimeException("상품 정보를 가져올 수 없습니다: " + item.getProductId());
//                        }
//
//                        // 재고 복구 todo : db만 복구 말고 레디스도 바꿔줘야 할 듯 ?? ....!!! 이거 선착순구매랑 연관 없게 7일뒤 복구해주거나...
                            // 만약 선착순도 진행하고 있는 중이여서 사람 몰리고 있는 상태에서 주문 취소에 대한 재고를
//                          레디스에서도 db 복구해준것처럼 재고 복구해주면 또 불일치 .....???


//                        int updatedStock = productDto.getStock() + item.getQuantity();
//                        productServiceClient.updateProductStock(productDto.getId(), updatedStock);
//                    }
//                    // 상태를 'RETURNED'로 변경
//                    order.setStatus(OrderStatus.RETURNED);
//                    orderRepository.save(order);
//                    log.info("주문 ID {}의 상태가 RETURNED로 변경 + 재고 업데이트 완료", order.getId());
//                } catch (Exception e) {
//                    log.error("주문 ID {}의 반품 처리 중 오류 발생: {}", order.getId(), e.getMessage());
//                    // 예외 처리 시 주문 상태 변경을 하지 않거나, 상태를 '취소 실패'로 설정하기
//                }
//            }
//        }*/
//
//        // 'RETURN_REQUESTED' 상태의 주문 목록 처리
//        List<Order> returnRequestedOrders = orderRepository.findByStatus(OrderStatus.RETURN_REQUESTED);
//        for (Order order : returnRequestedOrders) {
//            if (order.getReturnRequestDate().plusMinutes(1).isBefore(now)) {
//                try {
//                    // 재고 업데이트
//                    for (OrderItem item : order.getItems()) {
//                        ResponseEntity<ProductResponseDto> responseEntity = productServiceClient.getProductById(item.getProductId());
//                        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
//                            throw new RuntimeException("상품 정보를 가져올 수 없습니다: " + item.getProductId());
//                        }
//
//                        ProductResponseDto productDto = responseEntity.getBody();
//                        if (productDto == null) {
//                            throw new RuntimeException("상품 정보를 가져올 수 없습니다: " + item.getProductId());
//                        }
//
//                        // 재고 복구
//                        int updatedStock = productDto.getStock() + item.getQuantity();
//                        productServiceClient.updateProductStock(productDto.getId(), updatedStock);
//                    }
//
//                    // 상태를 'RETURNED'로 변경
//                    order.setStatus(OrderStatus.RETURNED);
//                    orderRepository.save(order);
//                    log.info("주문 ID {}의 상태가 RETURNED로 변경 + 재고 업데이트 완료", order.getId());
//
//                } catch (Exception e) {
//                    log.error("주문 ID {}의 반품 처리 중 오류 발생: {}", order.getId(), e.getMessage());
//                    throw new RuntimeException("반품 처리 중 오류 발생: " + e.getMessage());
//                }
//            }
//        }
//        log.info("스케줄러 종료 시간 : {}", LocalDateTime.now());
//    }
//}