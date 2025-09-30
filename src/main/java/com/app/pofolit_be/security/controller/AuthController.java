package com.app.pofolit_be.security.controller;

import com.app.pofolit_be.security.dto.TokenProperties;
import com.app.pofolit_be.security.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService authService;
    private final TokenProperties tokenProperties;

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @CookieValue("refreshToken") String refreshToken) {

        Map<String, String> tokens = authService.refreshAccessToken(refreshToken);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(tokenProperties.getRefreshTokenExp())
                .sameSite("None")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of("accessToken", tokens.get("accessToken")));
    }
}