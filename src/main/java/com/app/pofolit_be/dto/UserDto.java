package com.app.pofolit_be.dto;

public record UserDto(
        String email,
        String nickname,
        String profileImageUrl,
        String providerId
) { }
