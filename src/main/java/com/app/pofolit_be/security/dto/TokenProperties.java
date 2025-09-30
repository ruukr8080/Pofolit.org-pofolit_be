package com.app.pofolit_be.security.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "token")
public class TokenProperties {

    private String issuer;
    private String signingAlgorithm;
    private String secret;
    private Duration accessTokenExp;
    private Duration refreshTokenExp;

}