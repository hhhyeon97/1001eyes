package com.demo.userservice.dto;

import com.demo.userservice.core.EncryptionUtils;
import com.demo.userservice.model.Address;
import com.demo.userservice.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class UserDto {

    private Long userId;
    private String username;
    private String name;
    private String email;
    private String phone;
    private List<AddressDto> addresses; // AddressDto를 사용하여 복호화된 주소 정보를 저장

    // User 엔티티를 DTO로 변환하는 생성자
    public UserDto(User user) {
        this.username = user.getUsername();
        this.userId = user.getId();

        try {
            // EncryptionUtils를 사용해 복호화
            this.email = EncryptionUtils.decrypt(user.getEmail());
            this.phone = EncryptionUtils.decrypt(user.getPhone());
            this.name = EncryptionUtils.decrypt(user.getName());
        } catch (Exception e) {
            e.printStackTrace(); // 예외를 로그에 기록
            this.email = null;   // 복호화 실패 시 null로 설정하거나 기본값으로 설정
            this.phone = null;
        }

        // Address 객체들을 AddressDto로 변환하면서 복호화
        this.addresses = user.getAddresses().stream()
                .map(AddressDto::new) // 각 Address 객체를 AddressDto로 변환
                .collect(Collectors.toList());
    }


}
