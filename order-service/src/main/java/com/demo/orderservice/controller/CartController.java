package com.demo.orderservice.controller;


import com.demo.orderservice.dto.CartDto;
import com.demo.orderservice.dto.CartItemDto;
import com.demo.orderservice.model.Cart;
import com.demo.orderservice.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 카트 조회
    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader("X-Auth-User-ID") String userId) {
        try {
            // 사용자 ID로 카트 조회, 없으면 생성
            Cart cart = cartService.getOrCreateCart(userId);
            // 카트 아이템들을 CartDto로 변환
            CartDto cartDto = new CartDto();
            cartDto.setId(cart.getId());
            cartDto.setUserId(userId);
            cartDto.setTotalPrice(cart.getTotalPrice());
            cartDto.setItems(cart.getItems().stream().map(item -> {
                CartItemDto itemDto = new CartItemDto();
                itemDto.setId(item.getId());
                itemDto.setProductId(item.getProductId());
                itemDto.setQuantity(item.getQuantity());
                return itemDto;
            }).toList());
            return ResponseEntity.ok(cartDto);
        } catch (Exception e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니를 조회하는 중 오류가 발생했습니다.");
        }
    }

    // 카트에 상품 추가
    @PostMapping
    public ResponseEntity<String> addItemToCart(@RequestBody CartItemDto cartItemDto,
                                                @RequestHeader("X-Auth-User-ID") String userId) {
        try {
            // 장바구니에 아이템 추가
            cartService.addItemToCart(userId, cartItemDto.getProductId(), cartItemDto.getQuantity());
            return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
        } catch (RuntimeException e) {
            // 사용자 정의 예외 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 일반 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니에 아이템을 추가하는 중 오류가 발생했습니다.");
        }
    }

    // 카트에 상품 제거
    @DeleteMapping
    public ResponseEntity<String> removeItemFromCart(
            @RequestHeader("X-Auth-User-ID") String userId, // 헤더에서 사용자 ID 가져오기
            @RequestParam Long cartItemId) {
        try {
            // 장바구니에서 아이템 제거
            cartService.removeItemFromCart(userId, cartItemId);
            return ResponseEntity.ok("장바구니에서 상품이 삭제되었습니다.");
        } catch (RuntimeException e) {
            // 사용자 정의 예외 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 일반 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 삭제 중 오류가 발생했습니다.");
        }
    }

    // 카트 상품 수량 업데이트
    @PutMapping
    public ResponseEntity<String> updateCartItemQuantity(@RequestHeader("X-Auth-User-ID") String userId,
                                                         @RequestParam Long cartItemId,
                                                         @RequestParam Integer newQuantity) {
        try {
            cartService.updateCartItemQuantity(userId, cartItemId, newQuantity);
            return ResponseEntity.ok("장바구니 상품 수량이 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}