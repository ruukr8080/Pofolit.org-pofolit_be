package com.app.pofolit_be.security.service;

import com.app.pofolit_be.common.exception.CustomException;
import com.app.pofolit_be.common.exception.ExCode;
import com.app.pofolit_be.security.authentication.OIDCUser;
import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.mapper.OAuth2AttributeMapper;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * `OidcUserService`를 확장하여, User 엔티티를 생성/업뎃하고,
 * `SpringSecurity`가 사용할 `UserPrincipal`을 반환합니다.
 *
 * <p>
 * loadUser()에서 OIDC 인증 성공하고,
 * 받아낸 `refresh_token`을 user 에 저장합니다.
 * `AuthSuccessHandler`에서 Pofolit의 사용자 DB로 변환하기 위한 사전 작업입니다ㅣ.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignService extends OidcUserService {

    private final UserRepository userRepository;
    private final OAuth2AttributeMapper oAuth2AttributeMapper;

    @Override
    public OidcUser loadUser(OidcUserRequest oidcUserRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(oidcUserRequest);
        String providerId = oidcUser.getSubject();
        String registrationId = oidcUserRequest.getClientRegistration().getRegistrationId();

        SignDto userDto = oAuth2AttributeMapper.getOAuth2UserAttribute(
                registrationId,
                oidcUser.getAttributes()
        );
        User user = updateOrSaveUser(userDto);
        return OIDCUser.from(user, oidcUser);
    }

    public User updateOrSaveUser(SignDto signDto) {

        Optional<User> existingUser = userRepository.findByRegistrationIdAndProviderId(signDto.registrationId(), signDto.providerId());

        if(existingUser.isPresent()) {
            User user = existingUser.get();
            user.updateUser(signDto);
            return user;
        }
        // 이메일 충돌 검증
        Optional<User> existingUserEmail = userRepository.findUserByEmail(signDto.email());

        if(existingUserEmail.isPresent()) {
            throw new CustomException(ExCode.DUPLICATE_EMAIL);
        }
        return userRepository.save(signDto.toEntity());
    }
}