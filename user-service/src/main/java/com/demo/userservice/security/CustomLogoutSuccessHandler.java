package com.demo.userservice.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        // 로그아웃 성공 시 로그 출력
        logger.info("로그아웃 성공");

        // 응답 상태를 OK로 설정
        response.setStatus(HttpServletResponse.SC_OK);

        // 응답 본문에 로그아웃 성공 메시지 추가
        response.setContentType("application/json"); // JSON 형태로 응답을 보낼 경우
        response.setCharacterEncoding("UTF-8");

        // 응답 메시지 작성
        String responseMessage = "{\"message\":\"로그아웃 성공\"}";
        response.getWriter().write(responseMessage);
    }
}