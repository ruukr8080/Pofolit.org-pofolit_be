package com.app.pofolit_be.mapper.provider;

import com.app.pofolit_be.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KakaoAttributeMapper implements OAuth2AttributeMapper {

   @Override
   public UserDto mapToDto(Map<String, Object> attributes) {
      String providerId = String.valueOf(attributes.get("id"));
      Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
      Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

      String email = kakaoAccount.get("email") != null ? (String) kakaoAccount.get("email") : providerId + "@pofolit.com";

      return new UserDto(
              email,
              (String) profile.get("nickname"),
              (String) profile.get("profile_image_url"),
              providerId
      );
   }

   @Override
   public boolean supports(String registrationId) {
      return "kakao".equalsIgnoreCase(registrationId);
   }
}