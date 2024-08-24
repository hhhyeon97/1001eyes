package com.demo.userservice.controller;

import com.demo.userservice.dto.ChangePasswordRequestDto;
import com.demo.userservice.dto.RegisterRequestDto;
import com.demo.userservice.model.User;
import com.demo.userservice.security.UserDetailsImpl;
import com.demo.userservice.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 이메일 인증 코드 전송
    @PostMapping("/send")
    public ResponseEntity<String> sendVerificationCode(@RequestParam String email) {
        try {
            userService.sendVerificationCode(email);
            return ResponseEntity.ok("인증 코드가 이메일로 발송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 이메일 인증
    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String verificationCode) {
        try {
            String result = userService.verifyEmail(email, verificationCode);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto requestDto) {
        try {
            userService.join(requestDto);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 비밀번호 재설정
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequestDto requestDto,
                                                 HttpServletResponse httpServletResponse) {
        try {
            userService.changePassword(requestDto.getUsername(), requestDto.getOldPassword(), requestDto.getNewPassword(), httpServletResponse);
            return ResponseEntity.ok("비밀번호가 업데이트 되었습니다. 로그인 후 이용해 주세요.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //    // 유저 정보 조회
//    @GetMapping("/me")
//    public ResponseEntity<User> getUserDetails(@RequestHeader("X-Auth-User-ID") String userId) {
//        try {
//            // 현재 인증된 사용자 정보 가져오기
//            System.out.println("조회하려는 유저아이디 -> "+userId);
//            User currentUser = userService.findUserById(userId);
//            return ResponseEntity.ok(currentUser);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//    @GetMapping("/me")
//    public ResponseEntity<User> getUserDetails(@RequestHeader(value = "X-Auth-User-ID") String userId) {
//        try {
//            // userId를 통해 유저 정보 조회
//            User currentUser = userService.findUserById(userId);
//            return ResponseEntity.ok(currentUser);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }

}