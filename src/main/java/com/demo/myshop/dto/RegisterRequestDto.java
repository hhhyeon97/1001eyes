package com.demo.myshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegisterRequestDto {

    @NotBlank(message = "아이디를 입력해 주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;

    @Email(message = "유효한 이메일 주소를 입력해 주세요.")
    @NotBlank(message = "이메일을 입력해 주세요.")
    private String email;

    private String phone;
    private String name;
    private boolean admin = false;
    private String adminToken = "";

    private String address;
    private String addressDetail;
    private String zipcode;
    private boolean isDefault = true; // 기본 배송지 여부
    private String addressMessage = "배송 전 연락주세요.";
}