package com.app.pofolit_be.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final Environment env;

    public ResponseCookie createCookie(String name, String value, boolean httpOnly, long maxAgeSeconds) {
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("isProd");
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(isProd)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

    // 로그인 시 `내정보 기억하기였나` 설정
    public ResponseCookie createRememberMeCookie(String userId, long maxAgeSeconds) {
        return ResponseCookie.from("rememberMe", userId)
                .httpOnly(false)
                .secure(true)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    // 접근 가능 도메인 설정 : 어드민-도메인:"admin/~~.com..." / 유저도메인:"user/~~.com" / 프리뷰-도베인: "/"/
    public ResponseCookie createDomainCookie(String name, String value, String domain, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .domain(domain)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }
}