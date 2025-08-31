package com.app.pofolit_be.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("ROLE_GUEST", "미가입자"),
    USER("ROLE_USER", "유저");

    private final String key;
    private final String title;

    public static Role of(String key) {
        return Arrays.stream(Role.values())
                .filter(r -> r.getKey().equals(key))
                .findAny()
                .orElse(GUEST);
    }
}
