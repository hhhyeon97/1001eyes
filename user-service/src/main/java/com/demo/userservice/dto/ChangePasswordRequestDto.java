package com.demo.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangePasswordRequestDto {
    private String username;
    private String oldPassword;
    private String newPassword;
}