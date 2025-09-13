package com.app.pofolit_be.security.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
){}
