package com.app.pofolit_be.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    LV0("Lv0", "회원가입페이지 접근 가능", "최초 로그인"),
    LV1("Lv1", "일반 유저", "가입 이후 로그인함."),
    LV2("Lv2", "관리자", "관리자. 읽기 쓰기 수정"),
    LV3("Lv3", "사업자", "비지니스페이지 열람"),
    SYSTEM("SYSTEM", "서버 내부 통신용", "개발자");

    private final String lv;
    private final String access;
    private final String description;

    public static Role access(String lv) {
        for (Role level : values()) {
            if(level.access.equalsIgnoreCase(lv)) {
                return level;
            }
        }
        throw new IllegalArgumentException("접근 권한입없습니다.: " + lv);
    }
}
