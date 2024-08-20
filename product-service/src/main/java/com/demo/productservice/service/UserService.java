package com.demo.productservice.service;

import com.demo.productservice.client.UserServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserServiceClient userServiceClient;

    public UserService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public String test(String text) {
        System.out.println("여기는 유저 서비스이다 !!!");
        return userServiceClient.getResult(text);
    }

}
