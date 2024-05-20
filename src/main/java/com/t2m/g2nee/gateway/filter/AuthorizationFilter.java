package com.t2m.g2nee.gateway.filter;

import com.t2m.g2nee.gateway.util.JWTUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {
    private static final String CHECK_TOKEN = "Authorization";
    private static final String TOKEN_EXPIRED_MESSAGE = "토큰이 만료되었습니다.";
    private static final String TOKEN_INVALID_MESSAGE = "유효하지않은 토큰입니다";

    @RequiredArgsConstructor
    public static class Config {
        private final RedisTemplate<String, String> redisTemplate;
        private final JWTUtil jwtUtils;
    }

    /**
     * 인증 필터 생성자
     */
    public AuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(AuthorizationFilter.Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!checkHeaderAccessToken(request)) {
                String accessToken = Objects.requireNonNull(request.getHeaders()
                        .get(CHECK_TOKEN)).get(0).substring(7);
                log.info("현재 AccessToken: "+accessToken);

                //whiteList에 있는 accessToken인지 확인
                if (!checkWhiteList(config, accessToken)) {
                    log.info("whiteList에 있는 token과 일치하지않습니다.");
                    return makeResponse(exchange, TOKEN_INVALID_MESSAGE);
                }

                if (!config.jwtUtils.isValidateToken(accessToken)) {
                    log.info("만료기간이 지난 token입니다.");
                    return makeResponse(exchange, TOKEN_EXPIRED_MESSAGE);
                }

            }
            log.info("accessToken 통과");
            return chain.filter(exchange);
        });
    }

    /**
     * @param config      config 값 기입
     * @param accessToken accessToken 기입
     * @return boolean
     */
    private static boolean checkWhiteList(Config config, String accessToken) {
        String username = config.jwtUtils.getUsernameFromAccessToken(accessToken);
        String currentAccessToken =
                (String) config.redisTemplate.opsForHash().get("refreshToken:" + username, "currentAccessToken");

        //제일 최근에 발급된 accessToken인지
        if (!accessToken.equals(currentAccessToken)) {
            return false;
        }
        return true;
    }

    /**
     * Request Header 에 accessToken 이 있는지 확인하는 메서드입니다.
     *
     * @param request 현재의 요청이 기입
     * @return boolean
     */
    private static boolean checkHeaderAccessToken(ServerHttpRequest request) {
        return !(request.getHeaders().containsKey(CHECK_TOKEN));
    }

    /**
     * 화이트리스트에 해당 AccessToken이 없을때 UNAUTHORIZED 발생
     *
     * @param message
     * @return 401 error
     */
    private Mono<Void> makeResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        return response.setComplete();
    }
}
