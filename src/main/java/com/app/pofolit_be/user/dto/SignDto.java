package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;

/**
 * DB의 사용자 정보를 저장할 용도
 *
 * @param email
 * @param nickname
 * @param profileImageUrl
 * @param registrationId
 * @param providerId providerId의 역할은 여기서 끝.
 */
public record SignDto(
        String email,
        String nickname,
        String profileImageUrl,
        String registrationId,
        String providerId
)
{
    public User toEntity() {
        return User.builder()
                .email(this.email)
                .nickname(this.nickname)
                .profileImageUrl(this.profileImageUrl)
                .registrationId(registrationId)
                .providerId(providerId)
                .build();
    }

}
