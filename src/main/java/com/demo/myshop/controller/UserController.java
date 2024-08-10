package com.demo.myshop.controller;



import com.demo.myshop.dto.LoginRequestDto;
import com.demo.myshop.dto.RegisterRequestDto;
import com.demo.myshop.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    // 로그인
    @PostMapping("/login")
    public String login(LoginRequestDto requestDto, HttpServletResponse res){
        try {
            userService.login(requestDto, res);
        } catch (Exception e) {
            return "redirect:/api/user/login-form?error";
        }
        return "redirect:/";
    }
}