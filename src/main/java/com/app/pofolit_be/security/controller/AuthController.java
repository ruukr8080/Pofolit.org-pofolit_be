package com.app.pofolit_be.security.controller;

import com.app.pofolit_be.security.dto.TokenPair;
import com.app.pofolit_be.security.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenPair> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        log.info("/api/v1/auth/refresh [@PostMapping] [{}]", refreshToken);
        if(refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            TokenPair newTokenPair = tokenService.refreshAccessToken(refreshToken);
            long refreshExpSeconds = tokenService.getRefreshTokenExpirySeconds();
            ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken", newTokenPair.refreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshExpSeconds)
                    .sameSite("None") // CORS 환경 고려
                    .build();

            response.addHeader("Set-Cookie", newRefreshTokenCookie.toString());

            return ResponseEntity.ok(newTokenPair);

        } catch (JwtException e) {
            log.warn("리프레시토큰 검증 실패. {}", e.getMessage());
            ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "0")
                    .maxAge(0)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("None")
                    .build();
            response.addHeader("Set-Cookie", expiredCookie.toString());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}