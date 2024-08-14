package com.demo.myshop.security;

import com.demo.myshop.jwt.JwtUtilWithRedis;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
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
        System.out.println("token = " + token);
//        if (token != null) {
//            try {
//                // substringToken 호출
//                token = jwtUtilWithRedis.substringToken(token, request, response);
//                jwtUtilWithRedis.removeTokenFromRedis(token);
//            } catch (Exception e) {
//                logger.error("로그아웃 중 오류 발생: {}", e.getMessage());
//            }
//        }
        logger.info("로그아웃 성공");

        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("/"); // 로그아웃 후 메인 페이지로 리디렉션
    }
}