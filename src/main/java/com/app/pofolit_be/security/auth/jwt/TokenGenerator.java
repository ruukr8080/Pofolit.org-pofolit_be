package com.app.pofolit_be.security.auth.jwt;

import com.app.pofolit_be.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 토큰 생성, 검증, 파싱 로직을 담당하는 유틸리티 클래스
 * 로그인 후 JWT 토큰 발급해서 인증에 사용함
 */
@Slf4j
@Component
public class TokenGenerator {

   private final SecretKey key;
   private final long accessExp;
   private final long refreshExp;

   public TokenGenerator(@Value("${JWT_SECRET}") String secret,
                         @Value("${JWT_EXP}") long accessExp,
                         @Value("${REFRESH_JWT_EXP}") long refreshExp) {
      this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
      this.accessExp = accessExp * 1000;
      this.refreshExp = (refreshExp * 1000);
   }

   public String generateAccessToken(User user) {
      Map<String, Object> claims = new HashMap<>();
      claims.put("email", user.getEmail());
      claims.put("nickname", user.getNickname());
      claims.put("profileImageUrl", user.getProfileImageUrl());
      claims.put("role", user.getRole().getKey());
      return createToken(claims, user.getId().toString(), accessExp);
   }

   public String generateRefreshToken(User user) {
      return createToken(new HashMap<>(), user.getId().toString(), refreshExp);
   }

   private String createToken(Map<String, Object> claims, String subject, long EXP) {
      Date now = new Date();
      Date expiryDate = new Date(now.getTime() + EXP);
      return Jwts.builder()
              .claims(claims)
              .subject(subject)
              .issuedAt(now)
              .expiration(expiryDate)
              .signWith(key)
              .compact();
   }

   public UUID getUserIdFromToken(String token) {
      String subject = getClaimFromToken(token, Claims::getSubject);
      return UUID.fromString(subject);
   }

   public Date getExpirationDateFromToken(String token) {
      return getClaimFromToken(token, Claims::getExpiration);
   }

   public <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
      final Claims claims = getAllClaimsFromToken(token);
      return claimsResolver.resolve(claims);
   }

   public Claims getAllClaimsFromToken(String token) {
      return Jwts.parser()
              .verifyWith(key)
              .build()
              .parseSignedClaims(token)
              .getPayload();
   }

   public boolean validateToken(String token) {
      try {
         getAllClaimsFromToken(token);
         return !isTokenExpired(token);
      } catch (JwtException | IllegalArgumentException e) {
         log.error("JWT 토큰 검증 실패: {}", e.getMessage());
         return false;
      }
   }

   public boolean isTokenExpired(String token) {
      try {
         Date expiration = getExpirationDateFromToken(token);
         return expiration.before(new Date());
      } catch (JwtException e) {
         return true;
      }
   }

   @FunctionalInterface
   public interface ClaimsResolver<T> {
      T resolve(Claims claims);
   }
}
