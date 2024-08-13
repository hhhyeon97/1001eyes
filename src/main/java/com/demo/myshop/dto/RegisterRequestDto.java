package com.demo.myshop.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegisterRequestDto {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String name;
    private boolean admin = false;
    private String adminToken = "";

    private String address;
    private String addressDetail;
    private String zipcode;
    private boolean isDefaultAddress;
    private String addressPhoneNumber;
    private String addressMessage;
}