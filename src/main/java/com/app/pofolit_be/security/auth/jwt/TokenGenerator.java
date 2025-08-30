package com.app.pofolit_be.security.auth.jwt;

import com.app.pofolit_be.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Jwt Token 생성,유효성 검증, 정보 추출을 담당하는 유틸성 클래스입니다.
 * <p>
 * {@code @ConfigurationProperties}로 주입받습니다.
 * </p>
 *
 * @author 치킨
 * @apiNote JWT 토큰의 생명주기 관리와 관련된 모든 로직을 캡슐화합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenGenerator {

    private final TokenProperties tokenProperties;
    private SecretKey key;

    /**
     * Jwt 서명 키({@link #key})를 초기화합니다.
     * <p>
     * ({@link TokenProperties#secret()})를 {@code SecretKey} 객체로 변환하고
     * {@code HMAC-SHA} 알고리즘으로 인코딩합니다..
     * </p>
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(tokenProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * AccessToken 생성합니다.
     * <p>
     * 사용자의(email, nickname, profileImageUrl,role)를 {@code Claim}에 담습니다.
     * </p>
     *
     * @param user AccessToken 생성시 필요한 사용자의 entity 객체
     * @return 서명이 박힌 (String)AccessToken
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("nickname", user.getNickname());
        claims.put("profileImageUrl", user.getProfileImageUrl());
        claims.put("role", user.getRole().getKey());
        return createToken(claims, user.getId().toString(), tokenProperties.accessTokenExpOfSecond() * 1000);
    }

    /**
     * RefreshToken 생성합니다.
     * <p>
     * userId와 만료기간을 담아 {@code Map<String,String>}으로 반환합니다.
     * </p>
     *
     * @param user user
     * @return Map<userId, refreshTokenExp>
     */
    public String generateRefreshToken(User user) {
        return createToken(new HashMap<>(), user.getId().toString(), (int) tokenProperties.refreshTokenExpOfDay() * 1000 * 60 * 60 * 24);
    }

    /**
     * Claime을 파라미터로 받아, token을 생성합다.
     *
     * @param claims 사용자 정보
     * @param subject 인증에 썻던 userID
     * @param EXP 토큰 유효 기간 (초 단위)
     * @return Jwts 토큰키
     */
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

    /**
     * 토큰에서 userId를 추출합니다.
     *
     * @param token token
     * @return userId
     */
    public UUID getUserIdFromToken(String token) {
        String subject = getClaimFromToken(token, Claims::getSubject);
        return UUID.fromString(subject);
    }

    /**
     * 토큰 만료 날짜를 추출합니다.
     *
     * @param token token
     * @return 토큰 만료 날짜
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 특정 클레임 추출.
     *
     * @param token token
     * @param claimsResolver 추출할 클레임을 정의하는 함수형 인터페이스 입니다.
     * @param <T> 반환시킬 클레임의 타입
     * @return 추출한 클레임 값
     */
    public <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.resolve(claims);
    }

    /**
     * {@code key} 토큰의 유효성을 검증하고  모든 claims를 추출합니다.
     *
     * @param token token
     * @return 토큰에 있는 클레임 객체 전부.
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰의 유효성 검증을 합니다.
     * <p>
     * 서명여부,만료 여부를 확인합니다.
     * </p>
     *
     * @param token token
     * @return {@code true} : 유효  {@code false} : 무효
     */
    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (JwtException |
                 IllegalArgumentException e) {
            log.warn("JWT 토큰 검증 : {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰의 만료 여부를 확인합니다.
     * true가 반환되면 만료된 토큰입니다.
     *
     * @param token token
     * @return {@code true} : 만료 된,
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Claims 객체에서 특정 값 추출용 인터페이스입니다.
     */
    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}
