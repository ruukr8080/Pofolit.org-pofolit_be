package com.app.pofolit_be.common.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
   public void setValuesWithTimeout(String key,String value,long timeout) {
      redis.opsForValue().set(key, value, timeout, TimeUnit.MICROSECONDS);

   }
   // del
   public void deleteValues(String key) {
      redis.delete(key);
   }

}
