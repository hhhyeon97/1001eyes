package com.demo.orderservice.service;

import com.demo.orderservice.model.Order;
import com.demo.orderservice.model.OrderItem;
import com.demo.orderservice.model.OrderStatus;
import com.demo.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;
//    private final ProductRepository productRepository; // ProductRepository 추가

    public OrderStatusScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // 매일 자정에 주문 상태 업데이트 작업 실행
    // @Scheduled(cron = "0 0 0 * * ?")
    @Transactional  // 더티체킹을 위해 트랜잭션 추가
    @Scheduled(cron = "0 * * * * ?")  // test : 매 분마다 실행
    public void updateOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("스케줄러 시작 시간 : {}", now);

        // 'PENDING' 상태의 주문 목록 처리
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        for (Order order : pendingOrders) {
            if (order.getOrderDate().plusMinutes(1).isBefore(now)) {
                if (order.getStatus() != OrderStatus.SHIPPED) {
                    order.setStatus(OrderStatus.SHIPPED);
                    orderRepository.save(order);
                    log.info("주문 ID {}의 상태가 SHIPPED로 변경됨", order.getId());
                }
            }
        }

        // 'SHIPPED' 상태의 주문 목록 처리
        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);
        for (Order order : shippedOrders) {
            if (order.getOrderDate().plusMinutes(3).isBefore(now)) {
                if (order.getStatus() != OrderStatus.DELIVERED) {
                    order.setStatus(OrderStatus.DELIVERED);
                    order.setDeliveryDate(now);
                    orderRepository.save(order);
                    log.info("주문 ID {}의 상태가 DELIVERED로 변경 + 배송 완료 날짜가 {}로 설정됨", order.getId(), now);
                }
            }
        }

        // 'RETURN_REQUESTED' 상태의 주문 목록 처리
        List<Order> returnRequestedOrders = orderRepository.findByStatus(OrderStatus.RETURN_REQUESTED);
        for (Order order : returnRequestedOrders) {
            if (order.getReturnRequestDate().plusMinutes(1).isBefore(now)) {
                // 재고 업데이트
                for (OrderItem item : order.getItems()) {
//                    Product product = item.getProduct();
//                    product.setStock(product.getStock() + item.getQuantity());
//                    productRepository.save(product);
                    
                }
                // 상태를 'RETURNED'로 변경
                order.setStatus(OrderStatus.RETURNED);
                orderRepository.save(order);
                log.info("주문 ID {}의 상태가 RETURNED로 변경 + 재고 업데이트 완료", order.getId());
            }
        }
        log.info("스케줄러 종료 시간 : {}", LocalDateTime.now());
    }
}