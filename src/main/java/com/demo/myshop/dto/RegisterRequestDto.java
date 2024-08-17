package com.demo.myshop.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegisterRequestDto {

    private String username;
    private String password;

    @Email(message = "유효한 이메일 주소를 입력해 주세요.")
    private String email;

    private String phone;
    private String name; // 가입시에는 기본적으로 가입하는 사람 이름으로 받는 분 성함 컬럼에도 저장할 거임 !! (receiverName)
    private boolean admin = false;
    private String adminToken = "";

    private String address;
    private String addressDetail;
    private String zipcode;
    private boolean isDefaultAddress; // 기본 배송지 여부
    private String receiverPhoneNumber=""; // 받는 분 전화번호
    private String addressMessage="배송 전 연락주세요.";
}