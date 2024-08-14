package com.demo.myshop.repository;


import com.demo.myshop.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // 추가적인 쿼리 메서드 정의 가능
}