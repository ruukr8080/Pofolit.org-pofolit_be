package com.app.pofolit_be.user.dto.converter;

import com.app.pofolit_be.user.dto.OAuth2UserDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Google implements OAuth2Converter {

   @Override
   public OAuth2UserDto convertToDto(Map<String, Object> attributes, String registrationId) {
      String providerId = (String) attributes.get("sub");
      String email = (String) attributes.get("email");
      String nickname = (String) attributes.get("name");
      String profileImageUrl = (String) attributes.get("picture");

      return new OAuth2UserDto(email, nickname, profileImageUrl, providerId, registrationId);
   }
}