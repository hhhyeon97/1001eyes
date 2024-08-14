package com.demo.myshop.controller;

import com.demo.myshop.core.ApiUtils;
import com.demo.myshop.dto.ChangePasswordRequestDto;
import com.demo.myshop.dto.RegisterRequestDto;
import com.demo.myshop.jwt.JwtUtilWithRedis;
import com.demo.myshop.repository.UserRepository;
import com.demo.myshop.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtilWithRedis jwtUtilWithRedis;
    private final UserRepository userRepository;

    public UserController(UserService userService, JwtUtilWithRedis jwtUtilWithRedis, UserRepository userRepository) {
        this.userService = userService;
        this.jwtUtilWithRedis = jwtUtilWithRedis;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiUtils.ApiResult<String>> register(@RequestBody RegisterRequestDto requestDto) {
        try {
            userService.join(requestDto);
            return ResponseEntity.ok(ApiUtils.success("회원가입 요청이 완료되었습니다. 이메일 인증을 완료해 주세요."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String token) {
        try {
            // 이메일 복호화 및 인증 처리
            String result = userService.verifyEmail(email, token);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // 에러 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiUtils.ApiResult<String>> changePassword(@RequestBody ChangePasswordRequestDto requestDto) {
        try {
            userService.changePassword(requestDto.getUsername(), requestDto.getOldPassword(), requestDto.getNewPassword());
            return ResponseEntity.ok(ApiUtils.success("비밀번호 변경이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage()));
        }
    }

}