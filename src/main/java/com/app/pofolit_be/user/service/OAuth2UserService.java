package com.app.pofolit_be.user.service;

import com.app.pofolit_be.user.dto.CustomUserDetails;
import com.app.pofolit_be.user.dto.OAuth2UserDto;
import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

   private final UserRepository userRepository;



   @Override
   @Transactional
   public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
      OAuth2User oAuth2User = super.loadUser(userRequest);
      String registrationId = userRequest.getClientRegistration().getRegistrationId();
      Map<String, Object> attributes = oAuth2User.getAttributes();

      OAuth2UserDto userDto = toDto(registrationId, attributes);


      User user = userRepository.findByRegistrationIdAndProviderId(registrationId,userDto.providerId())
              .map(existingUser -> existingUser.update(userDto.nickname(),userDto.profileImageUrl()))
              .orElseGet(() -> userRepository.save(userDto.toEntity()));
      log.info("user save or update success@@@: {}", user);
      return new CustomUserDetails(user, oAuth2User.getAttributes());
   }

   private OAuth2UserDto toDto(String registrationId, Map<String, Object> attributes) {
      String providerId = (String) attributes.get("sub");
      String email = (String) attributes.get("email");
      String profileImageUrl = (String) attributes.get("picture");

      String nickname = getNickname(attributes) ;

      if(email == null && "kakao".equals(registrationId)){
         email = providerId + "@" + registrationId + ".com";
      }
      return new OAuth2UserDto(email, nickname, profileImageUrl, providerId, registrationId);
   }

   private String getNickname(Map<String, Object> attributes) {
      String nickname = (String) attributes.get("name");
      if (nickname == null) {
         // kakao
         nickname = (String) attributes.get("nickname");
      }
      return nickname;
   }
}