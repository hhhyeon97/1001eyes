package com.demo.userservice.client;

import com.demo.userservice.dto.CartCreateRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", url = "http://localhost:8083")
public interface CartServiceClient {

    @PostMapping("/api/carts")
    void createCart(@RequestBody CartCreateRequestDto cartRequestDto);
}
