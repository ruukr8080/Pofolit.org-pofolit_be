package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * UserResponseDTO
 *
 * @param email
 * @param nickname
 * @param profileImageUrl
 * @param birthDay
 * @param domain
 * @param job
 */
public record UserResponseDto(
        String email,
        String nickname,
        @JsonProperty("profileImageUrl")
        String profileImageUrl,
        LocalDate birthDay,
        @JsonProperty("birthDay")
        String domain,
        String job
)
{
    public static UserResponseDto from(User user) {
        return new UserResponseDto(user.getEmail(), user.getNickname(), user.getProfileImageUrl(), user.getBirthDay(), user.getDomain(), user.getJob());
    }
}

