package com.demo.apigateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;

@Component
public class JwtUtil {
    // 0. JWT 생성할 때 필요한 데이터

    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // Token 식별자 (규칙 같은 것! 토큰 앞에 붙일 것이고 한 칸 띄어쓰기 주의)
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;
    private Key key;

    // 로그 설정
    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

    @PostConstruct
    public void init() {
        // 현재 시크릿키가 Base64로 인코딩된 값이므로 사용하려면 Decoding 한 번 해주어야 함!
        // 반환 타입이 바이트 배열 타입임
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        // key 변수에 사용할 시크릿키를 담는다.
        key = Keys.hmacShaKeyFor(bytes);
    }


    // JWT 토큰 substring
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        logger.error("Not Found Token");
        throw new NullPointerException("토큰을 찾을 수 없습니다.");
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            logger.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // Cookie Value : JWT 가져오기
    public String getTokenFromRequest(ServerHttpRequest req) {
        HttpCookie cookie = req.getCookies().getFirst(AUTHORIZATION_HEADER);
        try {
            return URLDecoder.decode(cookie.getValue(), "UTF-8"); // Encode 되어 넘어간 Value 다시 Decode
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    public void removeJwtCookie(ServerHttpResponse response) {
        ResponseCookie cookie = ResponseCookie.from(AUTHORIZATION_HEADER, null)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ZERO)
                .build();
        response.addCookie(cookie);
    }

}
