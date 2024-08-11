package com.demo.myshop.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegisterRequestDto { // TODO : 주소 추가
    private String username;
    private String password;
    private String email;
    private String phone;
    private boolean admin = false;
    private String adminToken = "";
}