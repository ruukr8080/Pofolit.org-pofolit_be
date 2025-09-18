package com.app.pofolit_be.security.dto;

import io.jsonwebtoken.Claims;

public record AuthUserInfo(
        String sub,
        String providerId,
        String email,
        String nickname,
        String picture,
        String role
)
{
    public static AuthUserInfo fromPreTokenClaims(Claims claims) {
        return new AuthUserInfo(
                (String) claims.get("email"),
                (String) claims.get("nickname"),
                (String) claims.get("picture"),
                null,
                null,
                null
        );
    }
}