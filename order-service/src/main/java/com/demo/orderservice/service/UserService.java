package com.demo.orderservice.service;

import com.demo.orderservice.client.UserServiceClient;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserServiceClient userServiceClient;

    public UserService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public String test(String text) {
        System.out.println("여기는 유저 서비스이다 !!!(오더모듈에있는 유저서비스)");
        return userServiceClient.getResult(text);
    }
}
