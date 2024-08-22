package com.demo.orderservice.client;

import com.demo.orderservice.core.ApiUtils;
import com.demo.orderservice.dto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", url = "http://localhost:8082")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ApiUtils.ApiResult<ProductResponseDto> getProductById(@PathVariable("id") Long id);


//    @PostMapping("/api/products/{id}/stock")
//    void updateProductStock(@PathVariable("id") Long id, @RequestBody ProductResponseDto productDto);

    @PutMapping("/api/products/{id}/stock") // POST -> PUT으로 변경
    void updateProductStock(@PathVariable("id") Long id, @RequestBody ProductResponseDto productDto);
}