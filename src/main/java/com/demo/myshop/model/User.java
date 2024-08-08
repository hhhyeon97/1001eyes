package com.demo.myshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    private String phone;

    private boolean withdraw;

    private String role;

    @CreatedDate
    private LocalDateTime created_at;

    @LastModifiedDate
    private LocalDateTime updated_at;

    // 이메일 인증 관련 필드 추가
    private String authNum; // 인증번호
    private LocalDateTime authNumCreatedAt; // 인증번호 생성 시간
    private LocalDateTime authNumExpiresAt; // 인증번호 만료 시간
    private boolean isEmailVerified; // 이메일 인증 여부

}
