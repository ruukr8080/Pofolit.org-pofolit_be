package com.app.pofolit_be.security.authentication;

import com.app.pofolit_be.security.SecurityLevel;
import com.app.pofolit_be.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 관례상 네이밍은 `UserPrincipal`나 `CustomUserDetails`등 입니다.
 * 시쿠리티 사용자 정보를 담는 객체
 * <p>
 * - OIDC 기반 인증 {@link OidcUser}
 * </p>
 */
@Getter
public class AuthenticatedUser implements OidcUser {


    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;
    SecurityLevel accessLv;

    // OAuth2 전용 생성자
    private AuthenticatedUser(Map<String, Object> attributes,
                              OidcIdToken idToken,
                              OidcUserInfo userInfo) {
        this.attributes = attributes;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    // 로그인 안한 유저 / PreToken 용
    public static AuthenticatedUser PreTokenFrom(Map<String, Object> claims) {
        return new AuthenticatedUser(
                claims,
                null,
                null
        );
    }

    public static AuthenticatedUser TokenFrom(OidcUser oidcUser) {
        if(oidcUser.getUserInfo() == null || oidcUser.getUserInfo().getNickName().isBlank()) {
            System.out.println("oidcUser UserInfo가 없거나 닉넴이 없음.");
            // TODO: 신규 가입 직후 닉네임이 없을 수도 있음 닉네임 플래그 (세션 or 토큰 claim)
        }
        return new AuthenticatedUser(
                oidcUser.getAttributes(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }


    @Override
    public String getName() {
        return idToken != null ? idToken.getSubject() : (String) attributes.getOrDefault("sub", "anonymous");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // 유저 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = (String) attributes.getOrDefault("role", "ROLE_GUEST");
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
}


