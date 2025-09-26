package com.app.pofolit_be.security.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
)
{
}


