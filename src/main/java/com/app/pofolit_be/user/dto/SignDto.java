package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;

/**
 * SignDto
 *
 * @param email
 * @param nickname
 * @param profileImageUrl
 * @param registrationId
 * @param providerId
 * @param refreshToken
 */
public record SignDto(
        String email,
        String nickname,
        String profileImageUrl,
        String registrationId,
        String providerId,
        String refreshToken
)
{
    public User toEntity() {
        return User.builder()
                .email(this.email)
                .nickname(this.nickname)
                .profileImageUrl(this.profileImageUrl)
                .providerId(providerId)
                .registrationId(registrationId)
                .oauthRefreshToken(this.refreshToken)
                .role(Role.GUEST)
                .build();
    }
}
