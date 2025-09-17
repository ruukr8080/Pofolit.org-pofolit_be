package com.app.pofolit_be.user.entity;


import java.util.Arrays;

public enum Role {
    GUEST("ROLE_GUEST", "미가입자"),
    USER("ROLE_USER", "가입자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;

    Role(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public static Role of(String key) {
        return Arrays.stream(Role.values())
                .filter(r -> r.getKey().equals(key))
                .findAny()
                .orElse(GUEST);
    }
}
