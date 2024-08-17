package com.demo.myshop.service;

import com.demo.myshop.model.Cart;
import com.demo.myshop.model.CartItem;
import com.demo.myshop.model.Product;
import com.demo.myshop.repository.CartItemRepository;
import com.demo.myshop.repository.CartRepository;
import com.demo.myshop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> getCartItems(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("장바구니 정보를 찾을 수 없습니다."));
        return List.copyOf(cart.getItems());
    }

    public void addItemToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("장바구니 정보를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 상품 없음"));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        cart.getItems().add(cartItem); // 카트에 아이템 추가
        cart.updateTotalPrice(); // 총 가격 업데이트

        cartItemRepository.save(cartItem);
        cartRepository.save(cart); // 장바구니 업데이트
    }

    public void removeItemFromCart(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("장바구니 정보를 찾을 수 없습니다."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 내 상품을 찾을 수 없습니다."));

        if (!cart.getItems().contains(cartItem)) {
            throw new RuntimeException("장바구니에 속하지 않은 상품입니다.");
        }

        cart.getItems().remove(cartItem); // 카트에서 아이템 제거
        cart.updateTotalPrice(); // 총 가격 업데이트

        cartItemRepository.delete(cartItem);
        cartRepository.save(cart); // 장바구니 업데이트
    }

    public void updateCartItemQuantity(Long userId, Long cartItemId, Integer newQuantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("장바구니 정보를 찾을 수 없습니다."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 내 상품을 찾을 수 없습니다."));

        if (!cart.getItems().contains(cartItem)) {
            throw new RuntimeException("장바구니에 속하지 않은 상품입니다.");
        }

        cartItem.setQuantity(newQuantity);
        cart.updateTotalPrice(); // 총 가격 업데이트

        cartItemRepository.save(cartItem);
        cartRepository.save(cart); // 장바구니 업데이트
    }
}