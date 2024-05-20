package com.t2m.g2nee.gateway.config;

import com.t2m.g2nee.gateway.filter.AuthorizationFilter;
import com.t2m.g2nee.gateway.util.JWTUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RouteLocatorConfig {
    @Bean
    public RouteLocator shopRoute(RouteLocatorBuilder builder,
                                  RedisTemplate<String, String> redisTemplate,
                                  JWTUtil jwtUtil,
                                  AuthorizationFilter authorizationFilter) {
        return builder.routes()
                //hello 요청이 오면 lb(load balancing) //g2nee-shop으로 로드벨런싱
                //기본값 50:50
                .route("g2nee-shop",
                        p -> p.path("/api/*/shop/**")
                                .filters(f -> f.rewritePath("/api/*/shop/(?<segment>.*)", "/${segment}")
                                        .filter(tokenFilter(authorizationFilter, redisTemplate, jwtUtil)))
                                .uri("lb://G2NEE-SHOP/"))
                .build();
    }

    @Bean
    public RouteLocator authRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("g2nee-auth",
                        p -> p.path("/api/*/auth/**")
                                .uri("lb://G2NEE-AUTH/")
                )
                .build();
    }

    private GatewayFilter tokenFilter(AuthorizationFilter authorizationFilter,
                                      RedisTemplate<String, String> redisTemplate,
                                      JWTUtil jwtUtil) {
        return authorizationFilter.apply(
                new AuthorizationFilter.Config(redisTemplate, jwtUtil));
    }
}
