package com.app.pofolit_be.security.auth;

import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.dto.UserPrincipal;
import com.app.pofolit_be.user.mapper.OAuth2AttributeMapper;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignService extends OidcUserService {
   private final UserService userService;
   private final OAuth2AttributeMapper oAuth2AttributeMapper;

   @Override
   @Transactional
   public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
      OidcUser oidcUser = super.loadUser(userRequest);
      String registrationId = userRequest.getClientRegistration().getRegistrationId();
      SignDto userDto = oAuth2AttributeMapper.getOAuth2UserDto(registrationId, oidcUser.getAttributes());
      userService.updateOrSaveUser(userDto);
      return new UserPrincipal(userService.getUserByEmail(userDto.email()), oidcUser.getAttributes());
   }
}