package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * for only response  GET "api/v1/~me"
* */
public record UserResponseDto(
        String nickname,
        @JsonProperty("profileImageUrl")
        String profileImageUrl,
        LocalDate birthDay,
        @JsonProperty("birthDay")
        String domain,
        String job,
        List<String> interests
)
{
   public static UserResponseDto from(User user) {
      return new UserResponseDto(user.getNickname(), user.getProfileImageUrl(), user.getBirthDay(), user.getDomain(), user.getJob(), new ArrayList<>(user.getInterests()));
   }
}
