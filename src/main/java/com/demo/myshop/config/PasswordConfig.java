package com.demo.myshop.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    // Bean 어노테이션 추가
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Bean으로 등록하고싶은 객체를 반환하는 메서드 선언
        return new BCryptPasswordEncoder();
    }
}