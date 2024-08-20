package com.demo.userservice.controller;

import com.demo.userservice.core.jwt.JwtUtil;
import com.demo.userservice.dto.LoginRequestDto;
import com.demo.userservice.model.UserRoleEnum;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final JwtUtil jwtUtil; // JWT 유틸리티 클래스

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
//        // 인증 절차 (사용자 확인, 비밀번호 확인 등)
//        String username = authenticateUser(request.getUsername(), request.getPassword());
//
//        // JWT 토큰 생성
//        String token = jwtUtil.createToken(username);
//
//        // 응답 헤더에 JWT 토큰 추가
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Set-Cookie", "Authorization=" + token + "; Path=/; HttpOnly; Secure;");
//
//        return ResponseEntity.ok().headers(headers).body("로그인 성공");
//    }

    private String authenticateUser(String username, String password) {
        // 사용자 인증 로직 (DB 조회 등)
        return username; // 인증 성공 시 사용자 이름 반환
    }
}