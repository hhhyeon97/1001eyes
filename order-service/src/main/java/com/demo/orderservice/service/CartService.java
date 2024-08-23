package com.demo.orderservice.service;

import com.demo.orderservice.client.ProductServiceClient;
import com.demo.orderservice.dto.ProductResponseDto;
import com.demo.orderservice.model.Cart;
import com.demo.orderservice.model.CartItem;
import com.demo.orderservice.repository.CartItemRepository;
import com.demo.orderservice.repository.CartRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository
    , ProductServiceClient productServiceClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productServiceClient = productServiceClient;
    }

    public Cart getOrCreateCart(String userId) {
        // 사용자 ID로 카트 조회 또는 새 카트 생성
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setTotalPrice(0);
                    return cartRepository.save(newCart);
                });
    }

    public void addItemToCart(String userId, Long productId, Integer quantity) {
        // 사용자 ID로 장바구니 조회, 없으면 생성
        Cart cart = getOrCreateCart(userId);

        try {
            // 상품 정보 조회
            ResponseEntity<ProductResponseDto> responseEntity = productServiceClient.getProductById(productId);

            // 상품이 존재하지 않으면 예외 던지기
            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw new RuntimeException("해당 상품이 존재하지 않습니다.");
            }

            ProductResponseDto product = responseEntity.getBody();

            // 카트 아이템 조회
            CartItem existingCartItem = cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (existingCartItem != null) {
                // 기존 아이템 수량 증가
                existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            } else {
                // 새로운 아이템 추가
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProductId(productId);
                cartItem.setQuantity(quantity);
                cart.getItems().add(cartItem);
                cartItemRepository.save(cartItem); // 새로운 아이템 저장
            }

            // 총 가격 업데이트
            updateTotalPrice(cart);

            // 카트 저장
            cartRepository.save(cart);

        } catch (FeignException.NotFound e) {
            // FeignException.NotFound (404 에러)인 경우
            throw new RuntimeException("해당 상품이 존재하지 않습니다.");
        } catch (FeignException e) {
            // 기타 Feign 관련 에러 처리
            throw new RuntimeException("상품 정보를 조회하는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        } catch (Exception e) {
            // 일반 예외 처리
            throw new RuntimeException("장바구니에 아이템을 추가하는 중 오류가 발생했습니다.");
        }
    }

    public void removeItemFromCart(String userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("장바구니 정보를 찾을 수 없습니다."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니에서 상품을 찾을 수 없습니다."));

        if (!cart.getItems().contains(cartItem)) {
            throw new RuntimeException("장바구니에 속하지 않은 상품입니다.");
        }
        cart.getItems().remove(cartItem); // 카트에서 아이템 제거
        updateTotalPrice(cart); // 총 가격 업데이트

        cartItemRepository.delete(cartItem);
        cartRepository.save(cart); // 장바구니 업데이트
    }


    public void updateTotalPrice(Cart cart) {
        int totalPrice = cart.getItems().stream()
                .mapToInt(item -> {
                    ResponseEntity<ProductResponseDto> responseEntity = productServiceClient.getProductById(item.getProductId());
                    if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                        throw new RuntimeException("상품 정보 조회 중 오류가 발생했습니다.");
                    }
                    ProductResponseDto product = responseEntity.getBody();
                    return product.getPrice() * item.getQuantity();
                })
                .sum();
        cart.setTotalPrice(totalPrice);
    }

 /*


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
    }*/
}