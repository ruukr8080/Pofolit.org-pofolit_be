package com.app.pofolit_be.service;

import com.app.pofolit_be.dto.CustomUserDetails;
import com.app.pofolit_be.dto.UserDto;
import com.app.pofolit_be.entity.Role;
import com.app.pofolit_be.entity.User;
import com.app.pofolit_be.mapper.provider.OAuth2AttributeMapper;
import com.app.pofolit_be.repository.UserRepository;
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
   private final List<OAuth2AttributeMapper> attributeMappers;

   @Override
   @Transactional
   public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
      OAuth2User oAuth2User = super.loadUser(userRequest);
      String provider = userRequest.getClientRegistration().getRegistrationId();



      OAuth2AttributeMapper mapper = attributeMappers.stream()
              .filter(map -> map.supports(provider))
              .findFirst()
              .orElseThrow(() -> new OAuth2AuthenticationException("nono:{} " + provider));

      UserDto dto = mapper.mapToDto(oAuth2User.getAttributes());

      User user = userRepository.findByProviderId(dto.providerId())
              .map(existingUser -> existingUser.update(dto.nickname(), dto.profileImageUrl()))
              .orElse(User.builder()
                      .email(dto.email())
                      .nickname(dto.nickname())
                      .profileImageUrl(dto.profileImageUrl())
                      .provider(provider)
                      .providerId(dto.providerId())
                      .role(Role.USER)
                      .build());

      User savedUser =  userRepository.save(user);
      log.info("user save or update success@@@ {}",savedUser);

      return new CustomUserDetails(savedUser, oAuth2User.getAttributes());
   }
}
