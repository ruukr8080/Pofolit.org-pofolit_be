package com.app.pofolit_be.security.service;

import com.app.pofolit_be.security.authentication.AuthenticatedUser;
import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.mapper.OAuth2AttributeMapper;
import com.app.pofolit_be.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignService extends OidcUserService implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final AuthService authService;
    private final OAuth2AttributeMapper oAuth2AttributeMapper;
    @Value("${uri.auth.base}")
    private String targetUrl;

    @Override
    public OidcUser loadUser(OidcUserRequest oidcUserRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(oidcUserRequest);

        SignDto userDto = oAuth2AttributeMapper.getOAuth2UserAttribute(
                oidcUserRequest.getClientRegistration().getRegistrationId(),
                oidcUser.getAttributes()
        );
        userService.saveOrUpdateUser(userDto);
        log.info("User login success: {}", oidcUser.getEmail());

        return AuthenticatedUser.TokenFrom(oidcUser);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();
        Map<String, ResponseCookie> cookies = authService.issueAllTokens(authUser);
        cookies.values().forEach(cookie -> response.addHeader("Set-Cookie", cookie.toString()));

        response.sendRedirect(targetUrl);
    }
}
