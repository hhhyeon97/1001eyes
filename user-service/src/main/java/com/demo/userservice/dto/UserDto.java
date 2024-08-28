package com.demo.userservice.dto;

import com.demo.userservice.model.Address;
import com.demo.userservice.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDto {

    private String username;
    private Long userId;
    private String email;
    private String phone;
    private List<Address> addresses;

    // User 엔티티를 DTO로 변환하는 생성자
    public UserDto(User user) {
        this.username = user.getUsername();
        this.userId = user.getId();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.addresses = user.getAddresses();
    }
}
