package com.app.pofolit_be.user.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
  LV0("GUEST", "회원가입페이지 접근 가능", "최초 로그인"),
  LV1("USER", "일반 유저", "가입 이후 로그인함."),
  LV2("MANAGER", "관리자", "관리자. 읽기 쓰기 수정"),
  LV3("BUSINESS", "사업자", "비지니스페이지 열람"),
  SYSTEM("SYSTEM", "서버 내부 통신용", "개발자");

  private static final Map<String, Role> LV_MAP =
      Arrays.stream(values())
          .collect(
              Collectors.toUnmodifiableMap(role -> role.lv.toUpperCase(), Function.identity()));
  private final String lv;
  private final String access;
  private final String description;

  public static Optional<Role> findByLevel(String level) {
    if (level == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(LV_MAP.get(level.toUpperCase()));
  }
}