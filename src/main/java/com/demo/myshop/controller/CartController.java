package com.demo.myshop.controller;

import com.demo.myshop.model.Cart;
import com.demo.myshop.model.CartItem;
import com.demo.myshop.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

}