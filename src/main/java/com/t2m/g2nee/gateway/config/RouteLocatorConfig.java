package com.t2m.g2nee.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfig {
    @Bean
    public RouteLocator shopRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                //hello 요청이 오면 lb(load balancing) //g2nee-shop으로 로드벨런싱
                //기본값 50:50
                .route("g2nee-shop",
                        p -> p.path("/api/*/shop/**")
                                .uri("lb://G2NEE-SHOP/")
                )
                .build();
    }

    @Bean
    public RouteLocator authRoute(RouteLocatorBuilder builder){
        return builder.routes()
                .route("g2nee-auth",
                        p -> p.path("/api/*/auth/**").and()
                                .uri("http://G2NEE-AUTH:8080")
                        )
                .build();
    }
}
