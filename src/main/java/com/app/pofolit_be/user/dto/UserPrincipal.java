package com.app.pofolit_be.user.dto;

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
 * (String)
 *
 * @param 'registrationId' "google","kakao"
 * @param "nickname" google = "name" | kakao = "nickname"
 * @param "profileImageUrl" google = "profileImageUrl" | kakao = "profile_image_url"
 * 외에
 */
@Getter
public class UserPrincipal implements OidcUser, UserDetails {

   private User user;
   private Map<String, Object> attributes;

   public UserPrincipal(User user, Map<String, Object> attributes) {
      this.user = user;
      this.attributes = attributes;
   }

   @Override
   public String getName() {
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

   //   ===안쓰는거

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
      return null;
   }

   @Override
   public OidcIdToken getIdToken() {
      return null;
   }
}