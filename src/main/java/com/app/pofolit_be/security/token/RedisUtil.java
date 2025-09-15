package com.app.pofolit_be.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    private String rtKey(String userId, String jti) {
        return "RT:" + userId + ":" + jti;
    }

    public void setRT(String userId, String jti, String value, long ttlSeconds) {
        redisTemplate.opsForValue().set(rtKey(userId, jti), value, Duration.ofSeconds(ttlSeconds));
    }

    public String getRT(String userId, String jti) {
        return redisTemplate.opsForValue().get(rtKey(userId, jti));
    }

    public void deleteRT(String userId, String jti) {
        redisTemplate.delete(rtKey(userId, jti));
    }

    public boolean existsRT(String userId, String jti) {
        return redisTemplate.hasKey(rtKey(userId, jti));
    }
}
