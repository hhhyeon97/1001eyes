package com.demo.myshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String encryptedAddress; // 암호화된 기본 주소
    private String encryptedAddressDetail; // 암호화된 상세 주소
    private String encryptedZipcode; // 암호화된 우편 번호

    private boolean isDefault; // 기본 배송지 체크
    private String message=""; // 배송 메시지

    @ManyToOne
    @JoinColumn(name = "user_id") // 외래 키 열 이름 명시
    private User user;

    @OneToOne
    @JoinColumn(name = "receiver_id")
    private Receiver receiver; // Receiver와 1:1 관계

    // 생성자
    public Address(String encryptedAddress, String encryptedAddressDetail, String encryptedZipcode,
                   boolean isDefault, String message, User user, Receiver receiver) {
        this.encryptedAddress = encryptedAddress;
        this.encryptedAddressDetail = encryptedAddressDetail;
        this.encryptedZipcode = encryptedZipcode;
        this.isDefault = isDefault;
        this.message = message;
        this.user = user;
        this.receiver = receiver;
    }
}