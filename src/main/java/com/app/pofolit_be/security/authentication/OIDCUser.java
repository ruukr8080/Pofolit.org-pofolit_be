package com.app.pofolit_be.security.authentication;

import com.app.pofolit_be.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 관례상 네이밍은 `UserPrincipal`나 `CustomUserDetails`등 입니다.
 * 시쿠리티 사용자 정보를 담는 객체
 * <p>
 * - JWT 기반 인증 {@link UserDetails}
 * - OIDC 기반 인증 {@link OidcUser}
 * </p>
 */

@Getter
public class OIDCUser implements OidcUser, UserDetails {

    private final User user;
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    // JWT 인증용
    private OIDCUser(User user) {
        this.user = user;
        this.attributes = Collections.emptyMap();
        this.idToken = null;
        this.userInfo = null;
    }

    // OAuth2-OIDC 인증용
    private OIDCUser(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.user = user;
        this.attributes = attributes;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    public static OIDCUser from(User user) {
        return new OIDCUser(user);
    }

    public static OIDCUser from(User user, OidcUser oidcUser) {
        return new OIDCUser(user, oidcUser.getAttributes(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    @Override
    public String getName() { // token-claim = "subject"
        return user.getProviderId();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(user != null && user.getRole() != null) {
            return Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey()));
        }
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getClaims() {
        return this.attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return this.userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return this.idToken;
    }

}