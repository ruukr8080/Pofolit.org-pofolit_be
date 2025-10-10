package com.app.pofolit_be.user.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class UserDto {

  public record UserCreateRequest(
      String email,
      String nickname,
      String avatar,
      String provider,
      String subject,
      String role
  ) {

  }

  public record UserUpdateRequest(
      String nickname,
      String avatar
  ) {

  }

  public record UserResponse(
      String email,
      String nickname,
      String avatar
  ) {

  }
}
