package com.app.pofolit_be.user.dto;

public record OAuth2UserDto(
        String email,
        String nickname,
        String profileImageUrl,
        String registrationId,
        String providerId
)
{
}
