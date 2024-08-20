package com.demo.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url="http://localhost:8081")
public interface UserServiceClient {

    @GetMapping("/api/users/test/{text}")
    String getResult(@PathVariable String text);


}
