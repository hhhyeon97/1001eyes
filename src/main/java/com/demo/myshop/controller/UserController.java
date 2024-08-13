package com.demo.myshop.controller;

import com.demo.myshop.core.ApiUtils;
import com.demo.myshop.dto.LoginRequestDto;
import com.demo.myshop.dto.RegisterRequestDto;
import com.demo.myshop.jwt.JwtUtilWithRedis;
import com.demo.myshop.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtilWithRedis jwtUtilWithRedis;

    public UserController(UserService userService, JwtUtilWithRedis jwtUtilWithRedis) {
        this.userService = userService;
        this.jwtUtilWithRedis = jwtUtilWithRedis;
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<ApiUtils.ApiResult<String>> register(@RequestBody RegisterRequestDto requestDto) {
        try {
            userService.join(requestDto);
            return ResponseEntity.ok(ApiUtils.success("회원가입 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage()));
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiUtils.ApiResult<String>> login(@RequestBody LoginRequestDto loginRequestDto) {
        // 로그인 로직 구현
        // 예: 인증 성공 시 JWT 토큰 발급
        // 현재는 예시로 성공 메시지 반환
        return ResponseEntity.ok(ApiUtils.success("로그인 성공"));
    }

    // 인증 상태 체크
    @GetMapping("/check-auth")
    public ResponseEntity<Map<String, Boolean>> checkAuth(HttpServletRequest req) {
        String token = jwtUtilWithRedis.getTokenFromRequest(req);
        boolean isAuthenticated = token != null && jwtUtilWithRedis.isTokenActive(token);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAuthenticated", isAuthenticated);
        return ResponseEntity.ok(response);
    }

    // 이메일 전송
    @GetMapping("/send")
    public ResponseEntity<ApiUtils.ApiResult<String>> sendEmail(@RequestParam String email) {
        String result = userService.handleEmailVerification(email);
        if (result.contains("이미 가입된 유저입니다.")) {
            return ResponseEntity.badRequest().body(ApiUtils.error(result));
        } else {
            return ResponseEntity.ok(ApiUtils.success(result));
        }
    }

    // 이메일 인증 (추가 예제)
    @GetMapping("/verify")
    public ResponseEntity<ApiUtils.ApiResult<String>> verifyEmail(@RequestParam String email) {
        // 이메일 인증 로직 구현
        // 예: 인증 성공 시 메시지 반환
        return ResponseEntity.ok(ApiUtils.success("이메일 인증 성공"));
    }
}