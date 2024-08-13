package com.demo.myshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @OneToMany(mappedBy = "user")
    private List<Address> addresses;  // 하나의 사용자는 여러 주소를 가질 수 있음

    private boolean withdraw;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    private boolean isEmailVerified; // 이메일 인증 상태
    private String emailVerificationToken; // 이메일 인증 토큰

    @CreationTimestamp
    private LocalDateTime created_at;

    @UpdateTimestamp
    private LocalDateTime updated_at;

    public User(String username, String password, String email, String phone, UserRoleEnum role, boolean isEmailVerified, String emailVerificationToken) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.isEmailVerified = isEmailVerified;
        this.emailVerificationToken = emailVerificationToken;
    }
}
