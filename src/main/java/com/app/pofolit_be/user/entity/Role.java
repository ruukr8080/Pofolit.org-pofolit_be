package com.app.pofolit_be.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
   USER("ROLE_USER","유저"),
   ADMIN("ROLE_ADMIN","관리자");

   private final String key;
   private final String title;
}
