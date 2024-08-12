package com.demo.myshop.controller;



import com.demo.myshop.dto.LoginRequestDto;
import com.demo.myshop.dto.RegisterRequestDto;
import com.demo.myshop.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입 페이지
    @GetMapping("/register-form")
    public String registerPage() {
        return "register";
    }

    // 회원가입
    @PostMapping("/register")
    public String register(RegisterRequestDto requestDto) {
        userService.join(requestDto);
        return "redirect:/api/user/login-form";
    }

    // 로그인 페이지
    @GetMapping("/login-form")
    public String loginPage() {
        return "login";
    }

//
//    @PostMapping("/logout")
//    public void logout(HttpServletResponse response) {
//        Cookie cookie = new Cookie("Authorization", null);
//        cookie.setPath("/");
//        cookie.setMaxAge(0); // 쿠키를 즉시 만료시킴
//        response.addCookie(cookie);
//    }

}