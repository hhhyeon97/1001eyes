package com.demo.myshop.security;

import com.demo.myshop.jwt.JwtUtilWithRedis;
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
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            String username = ((UserDetailsImpl) authentication.getPrincipal()).getUsername();
            jwtUtilWithRedis.invalidateUserTokens(username, response);
            logger.info("레디스에서 토큰 제거 !!!", username);
        }
        logger.info("로그아웃 성공");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}