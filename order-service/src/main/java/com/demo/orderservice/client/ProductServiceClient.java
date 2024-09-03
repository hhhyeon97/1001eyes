package com.demo.orderservice.client;

import com.demo.orderservice.dto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ResponseEntity<ProductResponseDto> getProductById(@PathVariable("id") Long id);

    // 상품 상세페이지에서 보여줄 api에선 stock을 레디스에서 조회함
    // 이 api는 db에서 상품 재고 조회할 내부 api -> 다른 정보 말고 db에서 stock만 반환 받는 용도 !!
    @GetMapping("/api/products/internal/{id}")
    ResponseEntity<Integer> getProductByInternalId(@PathVariable("id") Long id);

//    // 재고 업데이트할 내부 api
//    @PutMapping("/api/products/{id}/stock")
//    ResponseEntity<String> updateProductStock(@PathVariable("id") Long id, @RequestParam("stock") int stock);

    // 새로운 재고 확인 및 차감 API
    @PostMapping("/api/products/{id}/deduct")
    ResponseEntity<String> checkAndDeductStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);
}