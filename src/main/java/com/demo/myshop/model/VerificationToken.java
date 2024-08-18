package com.demo.myshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String verificationCode;

    @Column(nullable = false)
    private LocalDateTime expiryDate; // 필드 이름 확인

    @Column(nullable = false)
    private boolean isVerified = false;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public VerificationToken(String email, String verificationCode, LocalDateTime expiryDate, User user) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.expiryDate = expiryDate;
        this.user = user;
        this.isVerified = false;
    }
}