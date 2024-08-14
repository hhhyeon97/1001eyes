package com.demo.myshop.jwt;

import com.demo.myshop.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtilWithRedis jwtUtilWithRedis;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(JwtUtilWithRedis jwtUtilWithRedis, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtilWithRedis = jwtUtilWithRedis;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
            throws ServletException, IOException {
        String tokenValue = jwtUtilWithRedis.getTokenFromRequest(req);

        if (StringUtils.hasText(tokenValue)) {
            try {
                // JWT 토큰 substring
                tokenValue = jwtUtilWithRedis.substringToken(tokenValue, req, res);
                log.info("Token: {}", tokenValue);

                if (!jwtUtilWithRedis.validateToken(tokenValue)) {
                    log.error("Token validation failed");
                    // 토큰 검증 실패 시 쿠키 제거
                    removeJwtCookie(res);
                    // 리디렉션 또는 에러 응답 처리 (필요에 따라 추가)
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                    return;
                }

                Claims info = jwtUtilWithRedis.getUserInfoFromToken(tokenValue);
                setAuthentication(info.getSubject());

            } catch (Exception e) {
                log.error("Error processing token: {}", e.getMessage());
                // 쿠키 제거 및 에러 응답 처리
                removeJwtCookie(res);
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token processing error");
                return;
            }
        }
        filterChain.doFilter(req, res);
    }

    // 쿠키 제거 메서드
    public void removeJwtCookie(HttpServletResponse res) {
        Cookie cookie = new Cookie(JwtUtilWithRedis.AUTHORIZATION_HEADER, null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키를 즉시 만료
        res.addCookie(cookie);
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}