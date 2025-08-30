package com.app.pofolit_be.user.dto;

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
}
