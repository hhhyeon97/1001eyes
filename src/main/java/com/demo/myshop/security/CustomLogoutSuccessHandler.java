package com.demo.myshop.security;

import com.demo.myshop.jwt.JwtUtil;
import com.demo.myshop.jwt.JwtUtilWithRedis;
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
    private final JwtUtilWithRedis jwtUtilWithRedis;

    public CustomLogoutSuccessHandler(JwtUtilWithRedis jwtUtilWithRedis) {
        this.jwtUtilWithRedis = jwtUtilWithRedis;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String token = jwtUtilWithRedis.getTokenFromRequest(request);
        if (token != null) {
            token = jwtUtilWithRedis.substringToken(token);
            jwtUtilWithRedis.removeTokenFromRedis(token);
        }

        logger.info("로그아웃 성공");

        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("/"); // 로그아웃 후 메인 페이지로 리디렉션
    }
}