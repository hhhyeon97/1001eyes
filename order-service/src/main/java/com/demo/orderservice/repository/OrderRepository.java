//package com.demo.orderservice.repository;
//
//import com.demo.orderservice.model.Order;
//import com.demo.orderservice.model.OrderStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface OrderRepository extends JpaRepository<Order, Long> {
//    List<Order> findByUserId(Long userId);
//    // 주문 상태에 따른 주문 리스트 조회
//    List<Order> findByStatus(OrderStatus status);
//}