package com.app.pofolit_be.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    // RedisTemplate을 주입받아 Redis 명령어를 실행합니다.
    private final RedisTemplate<String, String> redisTemplate;
    private String RtKey(String userId) {
        return "RT:" + userId;
    }


    /**
     * Refresh Token을 Redis에 저장합니다.
     * 키: "RT:" + userId
     * 값: Refresh Token 문자열
     * @param userId 사용자의 고유 ID
     * @param refreshToken 저장할 리프레시 토큰 값
     * @param ttl 만료 시간 (초 단위)
     */
    public void saveRefreshToken(String userId, String refreshToken, long ttl) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");

        redisTemplate.opsForValue().set(RtKey(userId), refreshToken, ttl, TimeUnit.SECONDS);
    }

    /**
     * userId로 Refresh Token을 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 조회된 리프레시 토큰 값, 없으면 null
     */
    public Optional<String> getRefreshToken(String userId) {
        Object val = redisTemplate.opsForValue().get(RtKey(userId));
        if (val instanceof String s) {
            return Optional.of(s);
        }
        return Optional.empty();
    }


    /**
     * Refresh Token을 Redis에서 삭제합니다.
     * @param userId 삭제할 사용자의 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteRefreshToken(String userId) {
        String key = "RT:" + userId;
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
}
