package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.User;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String email,
        String nickname,
        String profileImageUrl
)
{
   public static UserResponseDto from(User user){
      return new UserResponseDto(user.getId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl());
   }
}
