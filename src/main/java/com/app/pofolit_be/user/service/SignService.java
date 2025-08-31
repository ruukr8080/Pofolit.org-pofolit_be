package com.app.pofolit_be.user.service;

import com.app.pofolit_be.security.auth.UserPrincipal;
import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.mapper.OAuth2AttributeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Oauth2 api 서명 응답을 처리 후
 * `Oauth2 API`로부터 발급받은 사용자 정보를 추출하고 저장합니다.
 * 이 때 refreshToken도 추출하여 `UserPrincipal`로 전달합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignService extends OidcUserService {
    private final UserService userService;
    private final OAuth2AttributeMapper oAuth2AttributeMapper;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String oAuthRefreshToken = extractOAuthRefreshToken(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        SignDto userDto = oAuth2AttributeMapper.getOAuth2UserAttribute(
                registrationId,
                oidcUser.getAttributes(),
                oAuthRefreshToken
        );
        User user = userService.updateOrSaveUser(userDto);
        return new UserPrincipal(user, oidcUser.getAttributes());
    }

    private String extractOAuthRefreshToken(OidcUserRequest userRequest) {
        Object oAuth2RefreshToken = userRequest.getAdditionalParameters().get("refresh_token");
        return oAuth2RefreshToken != null ? oAuth2RefreshToken.toString() : null;
    }

}