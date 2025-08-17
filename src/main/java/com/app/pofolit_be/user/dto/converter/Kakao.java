package com.app.pofolit_be.user.dto.converter;

import com.app.pofolit_be.user.dto.OAuth2UserDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Kakao implements OAuth2Converter {

   @Override
   @SuppressWarnings("unchecked")
   public OAuth2UserDto convertToDto(Map<String, Object> attributes, String registrationId) {
      String providerId = String.valueOf(attributes.get("id"));

      Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
      Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

      // 아직 이메일 동의 권한 없음. @kakao.com으로 땜빵.
      String email = (String) kakaoAccount.get("email");
      if(email == null) {
         email = providerId + "@kakao.com";
      }

      String nickname = (String) profile.get("nickname");
      String profileImageUrl = (String) profile.get("profile_image_url");

      return new OAuth2UserDto(email, nickname, profileImageUrl, providerId, registrationId);
   }
}