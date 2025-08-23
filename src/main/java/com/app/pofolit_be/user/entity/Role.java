package com.app.pofolit_be.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
   GUEST("ROLE_GUEST", "미가입자"),
   USER("ROLE_USER", "유저");

   private final String key;
   private final String title;

   public static Role fromKey(String key) {
      for (Role role : Role.values()) {
         if(role.getKey().equals(key)) {
            return role;
         }
      }
      return null;
   }


}
