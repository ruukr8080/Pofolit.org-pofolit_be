package com.app.pofolit_be.security.auth.jwt;

import io.jsonwebtoken.*;
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
 * OAuth2 로그인 후 JWT 토큰 발급해서 인증에 사용함
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${JWT_SECRET}") String secret,
                   @Value("${JWT_EXP}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration * 1000; // 초를 밀리초로 변환
    }

    /**
     * JWT 토큰 생성하는 메서드
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param role 사용자 권한
     * @return 생성된 JWT 토큰
     */
    public String generateToken(UUID userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        return createToken(claims, userId.toString());
    }

    /**
     * 토큰 생성 내부 메서드
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public UUID getUserIdFromToken(String token) {
        String subject = getClaimFromToken(token, Claims::getSubject);
        return UUID.fromString(subject);
    }

    /**
     * JWT 토큰에서 이메일 추출
     * @param token JWT 토큰
     * @return 사용자 이메일
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("email", String.class));
    }

    /**
     * JWT 토큰에서 권한 정보 추출
     * @param token JWT 토큰
     * @return 사용자 권한
     */
    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    /**
     * JWT 토큰에서 만료 시간 추출
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * JWT 토큰에서 특정 Claim 추출
     */
    public <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.resolve(claims);
    }

    /**
     * JWT 토큰에서 모든 Claims 추출
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * JWT 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT 토큰 만료 여부 확인
     * @param token JWT 토큰
     * @return 만료 여부
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true; // 파싱 실패 시 만료된 것으로 간주 ㅇㅇ
        }
    }

    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}
