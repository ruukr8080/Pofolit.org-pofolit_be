package com.app.pofolit_be.security.dto;

import com.app.pofolit_be.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;

@Getter
public class OIDCUser extends DefaultOidcUser {
    private final User user;

    public OIDCUser(Collection<? extends GrantedAuthority> authorities,
                    OidcIdToken idToken,
                    OidcUserInfo userInfoMabeNull,
                    String nameAttributeKey,
                    User user) {
        super(authorities, idToken, null, nameAttributeKey);
        this.user = user;
    }
}
