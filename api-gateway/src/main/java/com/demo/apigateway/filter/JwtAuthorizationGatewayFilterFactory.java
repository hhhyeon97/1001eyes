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
                    log.error("Token Error");
                    // 응답 메시지 작성 및 상태 코드 설정
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                    String responseMessage = "{\"message\":\"쿠키가 만료되었습니다. 로그인 후 이용해 주세요\"}";
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

   /* // 인증 처리
    public void setAuthentication(String username) {
        Authentication authentication = createAuthentication(username);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }*/

    // Config 클래스는 필터 구성용 빈 클래스입니다.
    public static class Config {
        // 필요한 필터 구성 설정을 여기에 추가할 수 있습니다.
    }
}
