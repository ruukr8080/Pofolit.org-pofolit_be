package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;

public record UserDto(
        String email,
        String nickname,
        String avatar,
        String provider,
        String subject,
        String access
)
{
    public static UserDto from(User user) {
        return new UserDto(
                user.getEmail(),
                user.getNickname(),
                user.getAvatar(),
                user.getProvider(),
                user.getSubject(),
                user.getAccess().getLv());
    }

    public User toEntity() {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .avatar(avatar)
                .provider(provider)
                .subject(subject)
                .access(access == null ? Role.LV0 : Role.access(access)).build();
    }

    public void signupUser(User user) {
        user.completeSignup(nickname, avatar);
    }

    public void updateUser(User user) {
        user.updateProfile(nickname, avatar);
    }
}