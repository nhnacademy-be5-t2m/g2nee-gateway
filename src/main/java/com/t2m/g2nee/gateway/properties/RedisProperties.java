package com.t2m.g2nee.gateway.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConstructorBinding
@RequiredArgsConstructor
@Component
@ConfigurationProperties(prefix = "g2nee.redis")
public class RedisProperties {

    private String host;

    private int port;

    private String password;

    private int database;


}
