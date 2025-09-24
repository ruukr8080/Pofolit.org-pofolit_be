package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;

public record UserDto(
        String email,
        String nickname,
        String avatar,
        String provider,
        String subject,
        Role access
)
{
    // 최초 소셜로그인시.
    public User toUser() {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .avatar(avatar)
                .provider(provider)
                .subject(subject)
                .access(Role.LV0)
                .build();
    }
    // 최초 가입폼 제출시.
    public void signupUser(User user) {
        user.completeSignup(nickname, avatar);
    }

    public void updateUser(User user) {
        user.updateProfile(nickname, avatar);
    }
}
