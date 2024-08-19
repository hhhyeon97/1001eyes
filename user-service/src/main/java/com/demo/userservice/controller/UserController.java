package com.demo.userservice.controller;

import com.demo.userservice.core.ApiUtils;
import com.demo.userservice.dto.ChangePasswordRequestDto;
import com.demo.userservice.dto.RegisterRequestDto;
import com.demo.userservice.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 이메일 인증 코드 전송
    @PostMapping("/send")
    public ResponseEntity<ApiUtils.ApiResult<String>> sendVerificationCode(@RequestParam String email) {
        try {
            userService.sendVerificationCode(email);
            return ResponseEntity.ok(ApiUtils.success("인증 코드가 이메일로 발송되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiUtils.error(e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String verificationCode) {
        try {
            String result = userService.verifyEmail(email, verificationCode);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiUtils.ApiResult<String>> register(@Valid @RequestBody RegisterRequestDto requestDto) {
        try {
            userService.join(requestDto);
            return ResponseEntity.ok(ApiUtils.success("회원가입이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiUtils.ApiResult<String>> changePassword(@RequestBody ChangePasswordRequestDto requestDto,
                                                                     HttpServletResponse httpServletResponse) {
        try {
            userService.changePassword(requestDto.getUsername(), requestDto.getOldPassword(), requestDto.getNewPassword(), httpServletResponse);
            return ResponseEntity.ok(ApiUtils.success("비밀번호가 업데이트 되었습니다. 로그인 후 이용해 주세요."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage()));
        }
    }

}