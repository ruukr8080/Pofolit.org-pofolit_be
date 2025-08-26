package com.app.pofolit_be.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Role {
   GUEST("ROLE_GUEST", "미가입자"),
   USER("ROLE_USER", "유저");

   // Role : key  미리 map으로 매핑해서 O(1) 조회시간 단축.
   private static final Map<String, Role> KEY_MAP =
           Arrays.stream(values()).collect(Collectors.toMap(Role::getKey, Function.identity()));

   private final String key;
   private final String title;

   public static Role fromKey(String key) {
      Role role = KEY_MAP.get(key);
      if (role == null) {
         // null을 반환하면 NullPointerException의 원인이 됨. 명시적으로 예외를 던지는게 안전.
         throw new IllegalArgumentException("Unknown role key: " + key);
      }
      return role;
   }
}
