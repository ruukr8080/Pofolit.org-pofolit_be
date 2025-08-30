package com.app.pofolit_be.common.external;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 세션 데이터 레디스
 * pup sub
 */
@Service
public class RedisService {

    private final StringRedisTemplate redis;

    public RedisService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    //save
    public void saveValues(String key, String value) {
        redis.opsForValue().set(key, value);
    }

    //find - key
    public String findValues(String key) {
        String value = redis.opsForValue().get(key);
        System.out.println("value = " + value);
        return value;
    }

    public void setValuesWithTimeout(String key, String value, long timeout) {
        redis.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);

    }

    // del
    public void deleteValues(String key) {
        redis.delete(key);
    }

}
