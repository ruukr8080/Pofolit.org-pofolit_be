package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.security.auth.CookieUtil;
import com.app.pofolit_be.security.auth.jwt.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * &#064;RestController  : 토큰 관련 API를 처리합니다.
 * <p>사용자의 토큰을 갱신하는 인증 플로우를 담당합니다.</p>
 * <p>모든 엔드포인트는 /api/v1/auth 경로 아래에 위치합니다.</p>
 *
 * @author 치킨
 * @apiNote 클라이언트의 (Cookie):accessToken 만료 시 (Cookie):refreshToken을 참조하여 토큰 재발급합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final TokenService tokenService;

    /**
     * (Cookie):refreshToken을 참조하여 (Cookie):accessToken 토큰을 발급하고 (Cookie):refreshToken을 갱신합니다.
     *
     * <p>(Cookie):refreshToken 검증 - 성공 시 새로운 토큰 쌍을 발급하고, 쿠키에 다시 저장해줍니다.</p>
     * <p>(Cookie):refreshToken 검증 - 실패 시 기존 쿠키를 만료시키고 401 Unauthorized 상태를 반환합니다.</p>
     *
     * @param refreshToken 쿠키에 담긴 리프레시 토큰 (만료 또는 존재하지 않을 수 있음)
     * @param response HTTP 응답 객체. 새로운 토큰을 쿠키에 추가하는 데 사용됩니다.
     * @return 성공 시 200 OK,
     * 실패 시 401 Unauthorized
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<Void> refreshTokens(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if(refreshToken == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            Map<String, String> newTokens = tokenService.reIssueToken(refreshToken);
            String newAccessToken = newTokens.get("accessToken");
            String newRefreshToken = newTokens.get("refreshToken");

            CookieUtil.addTokensToCookie(response, newAccessToken, newRefreshToken);
            tokenService.responseTokenByCookie(response, newTokens.get("accessToken"));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            CookieUtil.expireCookie(response, "accessToken");
            CookieUtil.expireCookie(response, "refreshToken");

            return ResponseEntity.status(401).build();
        }
    }
}