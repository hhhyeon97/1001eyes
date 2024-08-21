package com.demo.orderservice.client;

import com.demo.orderservice.dto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "http://localhost:8082")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ProductResponseDto getProductById(@PathVariable("id") Long id);
}