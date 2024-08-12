package com.demo.myshop.security;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.core.Authentication;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // 로그아웃 성공 시 로그 출력
        logger.info("로그아웃 성공");

        // 클라이언트를 로그아웃 후 리디렉션할 URL을 설정
        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("/"); // 로그아웃 후 메인 페이지로 리디렉션
    }
}