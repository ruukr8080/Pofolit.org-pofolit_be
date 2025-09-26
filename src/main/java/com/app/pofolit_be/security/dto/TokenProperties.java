package com.app.pofolit_be.security.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "jwt")
public class TokenProperties {

    private String issuer;
    private long accessTokenExp;
    private long refreshTokenExp;
}

