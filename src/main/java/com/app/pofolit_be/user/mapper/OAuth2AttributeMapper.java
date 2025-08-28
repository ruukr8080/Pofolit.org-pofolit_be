package com.app.pofolit_be.user.mapper;

import com.app.pofolit_be.user.dto.SignDto;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class OAuth2AttributeMapper {

   /**
    * 등록 ID와 속성을 기반으로 OAuth2UserDto를 생성합니다.
    * @param registrationId OAuth2 제공자 등록 ID (예: "google", "kakao")
    * @param attributes 사용자 정보 속성 맵
    * @return 통일된 OAuth2UserDto 객체
    */
   public SignDto getOAuth2UserDto(String registrationId, Map<String, Object> attributes) {
      String email, nickname, profileImageUrl;
      String providerId = String.valueOf(attributes.get("sub"));
      String refreshToken = String.valueOf(attributes.get("sub"));

      switch (registrationId) {
         case "google":
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
            profileImageUrl = (String) attributes.get("picture");
            break;
         case "kakao":
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) profile.get("email");
            nickname = (String) profile.get("nickname");
            profileImageUrl = (String) profile.get("profile_image_url");
            break;
         default:
            throw new IllegalArgumentException("Unsupported registrationId: " + registrationId);
      }

      return new SignDto(email, nickname, profileImageUrl, registrationId, refreshToken,providerId);
   }
}