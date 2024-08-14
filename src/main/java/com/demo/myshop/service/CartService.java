package com.demo.myshop.service;

import com.demo.myshop.jwt.JwtUtilWithRedis;
import com.demo.myshop.model.Cart;
import com.demo.myshop.model.CartItem;
import com.demo.myshop.model.Product;
import com.demo.myshop.model.User;
import com.demo.myshop.repository.CartRepository;
import com.demo.myshop.repository.CartItemRepository;
import com.demo.myshop.repository.ProductRepository;
import com.demo.myshop.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final JwtUtilWithRedis jwtUtilWithRedis;
    private final HttpServletRequest request;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository, JwtUtilWithRedis jwtUtilWithRedis, HttpServletRequest request) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.jwtUtilWithRedis = jwtUtilWithRedis;
        this.request = request;
    }

    @Transactional
    public void addItemToCart(Long productId, int quantity) {
        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCart(user));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);
    }

    private User getCurrentUser() {
        String token = jwtUtilWithRedis.getTokenFromRequest(request);
        if (token == null || !jwtUtilWithRedis.validateToken(token)) {
            throw new IllegalStateException("Invalid token");
        }

        Claims claims = jwtUtilWithRedis.getUserInfoFromToken(token);
        String username = claims.getSubject();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }
}