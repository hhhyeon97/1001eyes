package com.demo.myshop.service;

import com.demo.myshop.model.Order;
import com.demo.myshop.model.OrderStatus;
import com.demo.myshop.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderStatusScheduler {


    private final OrderRepository orderRepository;

    public OrderStatusScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    // 매일 자정에 주문 상태 업데이트 작업 실행
    // @Scheduled(cron = "0 0 0 * * ?")
    // test : 매 분마다 주문 상태 업데이트 작업 실행
    @Scheduled(cron = "0 * * * * ?")
    public void updateOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Scheduler started at {}", now);

        // 'PENDING' 상태의 주문 목록 조회
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Order order : pendingOrders) {
            // 주문이 생성된지 1분 이상 지난 경우 'SHIPPED' 상태로 변경
            if (order.getOrderDate().plusMinutes(1).isBefore(now)) {
                if (order.getStatus() != OrderStatus.SHIPPED) {
                    order.setStatus(OrderStatus.SHIPPED);
                    orderRepository.save(order);
                    log.info("Order ID {} status updated to SHIPPED", order.getId());
                }
            }
        }

        // 'SHIPPED' 상태의 주문 목록 조회
        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);

        for (Order order : shippedOrders) {
            // 주문이 생성된지 3분 이상 지난 경우 'DELIVERED' 상태로 변경
            if (order.getOrderDate().plusMinutes(3).isBefore(now)) {
                if (order.getStatus() != OrderStatus.DELIVERED) {
                    order.setStatus(OrderStatus.DELIVERED);
                    orderRepository.save(order);
                    log.info("Order ID {} status updated to DELIVERED", order.getId());
                }
            }
        }

        log.info("Scheduler finished at {}", LocalDateTime.now());
    }

}