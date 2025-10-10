package com.app.pofolit_be.security.service;

import com.app.pofolit_be.security.dto.OIDCUser;
import com.app.pofolit_be.security.dto.TokenProperties;
import com.app.pofolit_be.user.dto.UserDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignService extends OidcUserService implements AuthenticationSuccessHandler {

  @Lazy
  private final TokenService tokenService;
  private final UserService userService;
  private final TokenProperties tokenProperties;
  @Value("${uri.auth.base}")
  String target; // 3000

  @Override
  @Transactional
  public OidcUser loadUser(OidcUserRequest oidcUserRequest) throws OAuth2AuthenticationException {
    OidcUser oidcUser = super.loadUser(oidcUserRequest);
    String provider = oidcUser.getIssuer().toString();
    String subject = oidcUser.getSubject();

    OidcIdToken idToken = oidcUser.getIdToken();
    UserDto signedUser = new UserDto(
        oidcUser.getEmail(),
        oidcUser.getNickName(),
        oidcUser.getPicture(),
        provider.substring(provider.lastIndexOf("/") + 1),
        subject,
        "Lv0"
    );

    User user = userService.getUserBySubject(subject);

    if (user == null) {
      user = userService.createUser(signedUser);
    } else {
      user.updateProfile(signedUser.nickname(), signedUser.avatar());
    }

    return new OIDCUser(
        oidcUser.getAuthorities(),
        idToken,
        null,
        oidcUserRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
            .getUserNameAttributeName(),
        user
    );
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException {
    OIDCUser oidcUser = (OIDCUser) authentication.getPrincipal();
    User user = oidcUser.getUser();

    String refreshToken = tokenService.issueAndStoreRefreshToken(user);
    ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(tokenProperties.getRefreshTokenExp())
        .sameSite("None")
        .build();

    response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    response.sendRedirect(target);
  }
}
