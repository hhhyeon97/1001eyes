package com.demo.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", url="http://localhost:8081")
public interface UserServiceClient {

}
