package com.app.pofolit_be.user.service;

import com.app.pofolit_be.user.dto.CustomUserDetails;
import com.app.pofolit_be.user.dto.OAuth2UserDto;
import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.dto.converter.OAuth2Converter;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

   private final UserRepository userRepository;
   private final List<OAuth2Converter> attributeMappers;

   @Override
   @Transactional
   public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
      OAuth2User oAuth2User = super.loadUser(userRequest);
      String registrationId = userRequest.getClientRegistration().getRegistrationId();

      OAuth2UserDto cDto = attributeMappers.stream()
              .filter(convert -> convert.getClass().getSimpleName().equalsIgnoreCase(registrationId))
              .findFirst()
              .orElseThrow(() -> new OAuth2AuthenticationException("Unsupported provider: " + registrationId))
              .convertToDto(oAuth2User.getAttributes(), registrationId);

      User user = userRepository.findByRegistrationIdAndProviderId(registrationId, cDto.providerId())
              .map(existingUser -> existingUser.update(cDto.nickname(), cDto.profileImageUrl()))
              .orElse(User.builder()
                      .email(cDto.email())
                      .nickname(cDto.nickname())
                      .profileImageUrl(cDto.profileImageUrl())
                      .registrationId(registrationId)
                      .providerId(cDto.providerId())
                      .role(Role.USER)
                      .build());
      User savedUser =  userRepository.save(user);
      log.info("user save or update success@@@ {}",savedUser);
      return new CustomUserDetails(savedUser, oAuth2User.getAttributes());
   }
}
