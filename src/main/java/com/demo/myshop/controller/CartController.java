package com.demo.myshop.controller;

import com.demo.myshop.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    @PostMapping("/add")
    public void addItemToCart(@RequestParam Long productId, @RequestParam int quantity) {
        cartService.addItemToCart(productId, quantity);
    }
}