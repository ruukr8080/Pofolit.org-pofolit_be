package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;
import io.jsonwebtoken.Claims;

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
        String job,
        Role role
)
{
    public static UserResponseDto from(User user) {
        return new UserResponseDto(user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getBirthDay(),
                user.getDomain(),
                user.getJob(),
                user.getRole()
        );
    }
    public static UserResponseDto fromPreTokenClaims(Claims claims) {
        return new UserResponseDto(
                (String) claims.get("email"),
                (String) claims.get("nickname"),
                (String) claims.get("picture"),
                null,
                null,
                null,
                null
        );
    }
}
