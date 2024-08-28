package com.demo.userservice.dto;

import com.demo.userservice.model.Address;
import com.demo.userservice.core.EncryptionUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {
    private Long id;
    private String address; // 복호화된 기본 주소
    private String addressDetail; // 복호화된 상세 주소
    private String zipcode; // 복호화된 우편 번호
    private boolean isDefault;
    private String message;

    public AddressDto(Address address) {
        this.id = address.getId();
        this.isDefault = address.isDefault();
        this.message = address.getMessage();

        try {
            // EncryptionUtils를 사용해 각 필드 복호화
            this.address = EncryptionUtils.decrypt(address.getEncryptedAddress());
            this.addressDetail = EncryptionUtils.decrypt(address.getEncryptedAddressDetail());
            this.zipcode = EncryptionUtils.decrypt(address.getEncryptedZipcode());
        } catch (Exception e) {
            e.printStackTrace(); // 예외를 로그에 기록
            this.address = null; // 복호화 실패 시 null로 설정하거나 기본값으로 설정
            this.addressDetail = null;
            this.zipcode = null;
        }
    }
}