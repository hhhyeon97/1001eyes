package com.demo.myshop.service;

import com.demo.myshop.model.Cart;
import com.demo.myshop.model.CartItem;
import com.demo.myshop.repository.CartItemRepository;
import com.demo.myshop.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        // 사용자의 장바구니를 조회
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user id: " + userId));
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long cartId) {
        // 장바구니에 담긴 상품 목록을 조회
        return cartItemRepository.findByCartId(cartId);
    }
}