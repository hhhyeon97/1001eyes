package com.demo.myshop.controller;


import com.demo.myshop.dto.RegisterRequestDto;
import com.demo.myshop.jwt.JwtUtil;
import com.demo.myshop.jwt.JwtUtilWithRedis;
import com.demo.myshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtilWithRedis jwtUtilWithRedis;

    public UserController(UserService userService,JwtUtilWithRedis jwtUtilWithRedis) {
        this.userService = userService;
        this.jwtUtilWithRedis = jwtUtilWithRedis;

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

    @GetMapping("/check-auth")
    public Map<String, Boolean> checkAuth(HttpServletRequest req) {
        String token = jwtUtilWithRedis.getTokenFromRequest(req);

        boolean isAuthenticated = false;
        if (token != null && jwtUtilWithRedis.isTokenActive(token)) {
            isAuthenticated = true;
        }

        Map<String, Boolean> response = new HashMap<>();
        response.put("isAuthenticated", isAuthenticated);

        return response;
    }
}