package com.app.pofolit_be.security.dto;

public record AuthUserInfo(
        String sub,
        String providerId,
        String email,
        String nickname,
        String picture,
        String role
)
{
}