package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;

public record OAuth2UserDto(
        String email,
        String nickname,
        String profileImageUrl,
        String providerId,
        String registrationId

)  {
   public User toEntity() {
      return User.builder()
              .email(email)
              .nickname(nickname)
              .profileImageUrl(profileImageUrl)
              .registrationId(registrationId)
              .providerId(providerId)
              .role(Role.USER) // 기본 역할은 USER로 설정
              .build();
   }
}
