package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.User;

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
        String profileImageUrl,
        LocalDate birthDay,
        String domain,
        String job
)
{
    public static UserResponseDto from(User user) {
        return new UserResponseDto(user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getBirthDay(),
                user.getDomain(),
                user.getJob());
    }
}
