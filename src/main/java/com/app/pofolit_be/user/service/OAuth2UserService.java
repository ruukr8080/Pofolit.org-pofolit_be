package com.app.pofolit_be.user.service;

import com.app.pofolit_be.user.dto.CustomUserDetails;
import com.app.pofolit_be.user.dto.OAuth2UserDto;
import com.app.pofolit_be.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
   private final UserService userService;

   @Override
   public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
      OAuth2User oAuth2User = super.loadUser(userRequest);
      String registrationId = userRequest.getClientRegistration().getRegistrationId();
      Map<String, Object> attributes = oAuth2User.getAttributes();
      OAuth2AccessToken accessToken = userRequest.getAccessToken();
      log.info("@@@@accessToken{}",accessToken);
      OAuth2UserDto userDto = toDto(registrationId, attributes);
      User user = userService.findOrSaveUser(userDto);
      return new CustomUserDetails(user, oAuth2User.getAttributes());
   }

   private OAuth2UserDto toDto(String registrationId, Map<String, Object> attributes) {
      String providerId = (String) attributes.get("sub");
      String email = (String) attributes.get("email");
      String profileImageUrl = (String) attributes.get("picture");
      String nickname = registrationId.equals("google") ? (String) attributes.get("name") : (String) attributes.get("nickname");
      return new OAuth2UserDto(email, nickname, profileImageUrl, providerId, registrationId);
   }

}