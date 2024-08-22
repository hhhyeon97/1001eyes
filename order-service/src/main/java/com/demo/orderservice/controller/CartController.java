package com.demo.orderservice.controller;


import com.demo.orderservice.dto.CartCreateRequestDto;
import com.demo.orderservice.dto.CartDto;
import com.demo.orderservice.dto.CartItemDto;
import com.demo.orderservice.dto.OrderDto;
import com.demo.orderservice.model.CartItem;
import com.demo.orderservice.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<String> createCart(@RequestBody CartCreateRequestDto cartRequestDto) {
        cartService.createCart(cartRequestDto);
        return ResponseEntity.ok("장바구니 생성 완료");
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader("X-Auth-User-ID") String userId) {
        List<CartItem> cartItems = cartService.getCartItems(userId);

        if (cartItems.isEmpty()) {
            return ResponseEntity.ok("카트가 비어있습니다!");
        } else {
            // 카트 아이템들을 CartDto로 변환
            CartDto cartDto = new CartDto();
            cartDto.setId(cartItems.get(0).getCart().getId());
            cartDto.setUserId(userId);
            cartDto.setTotalPrice(cartItems.get(0).getCart().getTotalPrice());
            cartDto.setItems(cartItems.stream().map(item -> {
                CartItemDto itemDto = new CartItemDto();
                itemDto.setId(item.getId());
//                itemDto.setProductId(item.getProduct().getId());
                itemDto.setQuantity(item.getQuantity());
                return itemDto;
            }).toList());
            return ResponseEntity.ok(cartDto);
        }
    }
//
//    @PostMapping
//    public ResponseEntity<String> addItemToCart(@RequestBody CartItemDto cartItemDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        Long userId = userDetails.getUser().getId();
//        try {
//            cartService.addItemToCart(userId, cartItemDto.getProductId(), cartItemDto.getQuantity());
//            return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @DeleteMapping
//    public ResponseEntity<String> removeItemFromCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
//                                                     @RequestParam Long cartItemId) {
//        Long userId = userDetails.getUser().getId();
//        try {
//            cartService.removeItemFromCart(userId, cartItemId);
//            return ResponseEntity.ok("장바구니에서 상품이 삭제되었습니다.");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PutMapping
//    public ResponseEntity<String> updateCartItemQuantity(@AuthenticationPrincipal UserDetailsImpl userDetails,
//                                                         @RequestParam Long cartItemId,
//                                                         @RequestParam Integer newQuantity) {
//        Long userId = userDetails.getUser().getId();
//        try {
//            cartService.updateCartItemQuantity(userId, cartItemId, newQuantity);
//            return ResponseEntity.ok("장바구니 상품 수량이 업데이트되었습니다.");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}