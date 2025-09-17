package com.app.pofolit_be.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SecurityLevel {
    LV0("Lv0", "All User", "읽기"),
    LV1("Lv1", "로그인 사용자", "읽기 쓰기"),
    LV2("Lv2", "게시물 소유자", "읽기 쓰기 수정"),
    LV3("Lv3", "사업자", "비지니스페이지 열람"),
    SYSTEM("SYSTEM", "서버 내부 통신용", "개발자");

    private final String lv;
    private final String access;
    private final String description;

    public static SecurityLevel fromLv(String lv) {
        for (SecurityLevel level : values()) {
            if(level.access.equalsIgnoreCase(lv)) {
                return level;
            }
        }
        throw new IllegalArgumentException("접근 권한입없습니다.: " + lv);
    }
}
