package com.demo.myshop.service;

import com.demo.myshop.model.Order;
import com.demo.myshop.model.OrderStatus;
import com.demo.myshop.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;

    public OrderStatusScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
    public void updateOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Order order : orders) {
            // 주문이 생성된지 1일 이상 지난 경우 상태를 '배송중'으로 변경
            if (order.getOrderDate().plusDays(1).isBefore(now)) {
                order.setStatus(OrderStatus.SHIPPED);  // '배송중' 상태로 변경
                orderRepository.save(order);
            }
            // 주문이 생성된지 2일 이상 지난 경우 상태를 '배송완료'로 변경
            if (order.getOrderDate().plusDays(2).isBefore(now)) {
                order.setStatus(OrderStatus.DELIVERED);  // '배송완료' 상태로 변경
                orderRepository.save(order);
            }
        }
    }
}