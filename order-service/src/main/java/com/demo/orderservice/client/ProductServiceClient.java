package com.demo.orderservice.client;

import com.demo.orderservice.dto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", url = "http://localhost:8082")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ResponseEntity<ProductResponseDto> getProductById(@PathVariable("id") Long id);

    // 재고 업데이트 API
    @PutMapping("/api/products/{id}/stock")
    ResponseEntity<String> updateProductStock(@PathVariable("id") Long id, @RequestParam("stock") int stock);

}