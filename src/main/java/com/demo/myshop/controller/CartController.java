package com.demo.myshop.controller;

import com.demo.myshop.model.Cart;
import com.demo.myshop.model.User;
import com.demo.myshop.security.UserDetailsImpl;
import com.demo.myshop.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        List<Cart> cartItems = cartService.getCartByUserId(user.getId());

        if (cartItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body("카트가 비어있습니다!");
        } else {
            return ResponseEntity.ok(cartItems);
        }

    }

}