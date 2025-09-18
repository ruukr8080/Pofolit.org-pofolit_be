package com.app.pofolit_be.security.authentication;

import com.app.pofolit_be.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class AuthenticatedUser implements OidcUser {

    /**
     * OidcUserRequest: 최상위 객체
     * OidcIdToken: Map<String, Object> claims, subject expiresAt issuedAt (JWT).
     * OidcUserInfo: 사용자의 프로필에 대한 상세 정보 (JSON).
     * OidcUser: 위 둘을 하나로 통합한 시큐리티 객체.
     */
    private final User user;                // 우리 서비스 DB의 User 엔티티
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;


    //(DB 유저 + OIDC 정보)
    public AuthenticatedUser(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.user = user;
        this.attributes = attributes;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    // OidcUserRequest만 받음
    public AuthenticatedUser(OidcUserRequest request) {
        this.user = null;
        this.idToken = request.getIdToken();
        this.userInfo = null; // loadUser에서 없는 userInfo
        this.attributes = request.getClientRegistration()
                .getProviderDetails()
                .getConfigurationMetadata();
    }
    // JWT에서 로드할 때 쓰는 정적 팩토리 메서드
    public static AuthenticatedUser fromUserEntity(User user) {
        return new AuthenticatedUser(
                user,
                Collections.emptyMap(),
                null,
                null
        );
    }
    // 익명사용자용 팩토리 메서드
    public static AuthenticatedUser anonymous() {
        return new AuthenticatedUser(
                null,
                Collections.emptyMap(),
                null,
                null
        );
    }
    @Override
    public Map<String, Object> getClaims() {
        // claims,tokenvalue,exp,iat
        return idToken != null ? idToken.getClaims() : Collections.emptyMap();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String secureLv = user != null ? user.getSecurityLevel().getLv() : "Lv0";
        return Collections.singletonList(new SimpleGrantedAuthority(secureLv));
    }

    @Override
    public String getName() {
        return user != null ? user.getProviderId() : (String) getClaims().get("sub");
    }

    public boolean isOidcLogin() {
        return this.idToken != null;
    }
}