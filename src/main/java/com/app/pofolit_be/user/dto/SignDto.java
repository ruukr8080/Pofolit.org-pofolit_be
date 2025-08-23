package com.app.pofolit_be.user.dto;

public record SignDto(
        String email,
        String nickname,
        String profileImageUrl,
        String registrationId,
        String providerId
)
{
}
