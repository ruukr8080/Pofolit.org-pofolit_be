package com.app.pofolit_be.dto;

import com.app.pofolit_be.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements OAuth2User {

   private final User user;
   private final Map<String, Object> attributes;

   @Override
   public Map<String, Object> getAttributes() {
      return attributes;
   }

   @Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
      return Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey()));
   }

   @Override
   public String getName() {
      return user.getProviderId();
   }
}