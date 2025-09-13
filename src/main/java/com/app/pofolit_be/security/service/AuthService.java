package com.app.pofolit_be.security.service;

import com.app.pofolit_be.security.token.CookieUtil;
import com.app.pofolit_be.security.token.JwtUtil;
import com.app.pofolit_be.security.token.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final CookieUtil cookieUtil;

    /**
     * 로그아웃 처리
     * 1. 리프레시 토큰에서 userId 추출
     * 2. Redis에서 리프레시 토큰 삭제
     * 3. 액세스 토큰과 리프레시 토큰 쿠키 만료
     *
     * @param refreshToken 클라이언트의 리프레시 토큰
     * @param response HTTP 응답 객체
     */
    public void logout(String refreshToken, HttpServletResponse response) {
        try {
            // 리프레시 토큰이 있고 유효한 경우에만 Redis에서 삭제
            if (refreshToken != null && !refreshToken.trim().isEmpty()) {
                if (jwtUtil.isValid(refreshToken)) {
                    String userId = jwtUtil.getSubject(refreshToken);
                    boolean deleted = redisUtil.deleteRefreshToken(userId);
                    log.info("사용자 {} 리프레시 토큰 Redis 삭제 결과: {}", userId, deleted);
                } else {
                    log.warn("만료되거나 유효하지 않은 리프레시 토큰으로 로그아웃 시도");
                }
            }
        } catch (Exception e) {
            log.error("리프레시 토큰 처리 중 오류 발생: {}", e.getMessage());
        } finally {
            // 토큰 처리 결과에 관계없이 항상 쿠키는 만료시킴
            cookieUtil.expireCookie(response, "accessToken");
            cookieUtil.expireCookie(response, "refreshToken");
            log.info("로그아웃 완료: 쿠키 만료 처리됨");
        }
    }
}


