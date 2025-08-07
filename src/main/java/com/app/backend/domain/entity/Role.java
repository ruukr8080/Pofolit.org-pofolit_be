package com.app.backend.domain.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER("ROLE_USER", "unregistered"),
    AUTHOR("ROLE_AUTHOR","registered");

    private final String key;
    private final String status;

}
