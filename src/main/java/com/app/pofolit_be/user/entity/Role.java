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

    private static final Map<String, Role> KEY_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(Role::getKey, Function.identity()));

    private final String key;
    private final String title;

    public static Role fromKey(String key) {
        Role role = KEY_MAP.get(key);
        if(role == null) {
            throw new IllegalArgumentException("Unknown role key: " + key);
        }
        return role;
    }
}
