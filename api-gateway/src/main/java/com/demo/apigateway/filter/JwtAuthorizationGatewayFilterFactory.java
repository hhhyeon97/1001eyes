package com.demo.apigateway.filter;

import com.demo.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthorizationGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtAuthorizationGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String tokenValue = jwtUtil.getTokenFromRequest(exchange.getRequest());
            log.info("토큰 값 ",tokenValue);

            if (StringUtils.hasText(tokenValue)) {
                // JWT 토큰 substring
                tokenValue = jwtUtil.substringToken(tokenValue);
                log.info(tokenValue);

                if (!jwtUtil.validateToken(tokenValue)) {
                    log.error("Expired JWT token, 로그아웃 처리됩니다.");
                    // 쿠키 제거 메서드 호출
                    jwtUtil.removeJwtCookie(exchange.getResponse());
                    // 클라이언트에서 추가적인 처리 유도 (ex: 재로그인 메시지)
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    String responseMessage = "{\"message\":\"토큰이 만료되었습니다. 다시 로그인해 주세요.\"}";
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(responseMessage.getBytes())));
                }
                Claims info = jwtUtil.getUserInfoFromToken(tokenValue);
                String userId = info.getSubject();
                log.info("유저 정보: {}", userId);
                try {
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("X-Auth-User-ID", userId)
                                    .build())
                            .build();
                    return chain.filter(modifiedExchange);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                }
            }
            return exchange.getResponse().setComplete();
        };
    }
    // Config 클래스는 필터 구성용 빈 클래스입니다.
    public static class Config {
        // 필요한 필터 구성 설정을 여기에 추가할 수 있습니다.
    }
}
