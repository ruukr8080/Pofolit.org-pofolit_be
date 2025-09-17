package com.app.pofolit_be.security.token;

import com.app.pofolit_be.security.SecurityLevel;
import com.app.pofolit_be.security.authentication.AuthenticatedUser;
import com.app.pofolit_be.security.config.JwtProperties;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.service.UserService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.service.SecurityService;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.*;

/**
 * TODO:멀티 디바이스 관리,성능 최적화,
 * 멀티 디바이스 관리는 나중에 구현할 기능.
 * 성능최적화 :
 *                                장점	                             || 단점
 * 1. JWT 전체 저장 :	            단순 구현, 서버에서 전체 검증 가능  || 	메모리 사용 ↑
 * 2. jti / 최소 정보만 저장: 	    Redis 사용량 최소화, 로그아웃 관리	|| 전체 JWT 검증 불가, 일부 로직 필요
 * 3. 서명/Hash만 저장:          	메모리 절약, 성능 ↑	              || 구현 복잡, Signature 재계산 필요
 * 4. random token + Redis 매핑:	Stateless 유지, 서버 관리 용이	    ||  JWT payload 최소화 필요, 구현 약간 복잡
 * 5. 블랙리스트:                  Redis사용 최소,엑세스코큰 그대로씀 ||	만료 전까지 블랙리스트 유지 필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenUtil {

    private final JwtProperties jwtProperties;
    private final RedisUtil redisUtil;
    private final SecurityService securityParser;
    private final TokenValidator validator;
    public PrivateKey privateKey;

    public String generateRefreshToken(AuthenticatedUser authUser, SecurityLevel securityLevel) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.rTtl());
        String jti = UUID.randomUUID().toString();

        String refreshToken = Jwts.builder()
                .subject(authUser.getName())
                .issuer(jwtProperties.iss())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .id(jti)
                .audience().add(securityLevel.getLv()).and()
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        redisUtil.setRT(authUser.getName(), jti, refreshToken, jwtProperties.rTtl());

        return refreshToken;
    }

    public String generateAccessToken(AuthenticatedUser authUser, SecurityLevel securityLevel) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.aTtl());
        String jti = UUID.randomUUID().toString();

        Map<String, Object> claims = Map.of(
                "email", authUser.getAttributes().get("email"),
                "nickname", authUser.getAttributes().get("nickname"),
                "picture", authUser.getAttributes().get("picture"),
                "role", securityLevel.getLv()
        );
        return Jwts.builder()
                .subject(authUser.getName())
                .issuer(jwtProperties.iss())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .id(jti)
                .claims(claims)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generatePreToken(AuthenticatedUser authUser) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.pTtl());

        Map<String, Object> claims = Map.of(
                "email", Optional.ofNullable(authUser.getIdToken())
                        .map(id -> id.getClaim("email"))
                        .orElse("anonymousMail"),
                "nickname", Optional.ofNullable(authUser.getIdToken())
                        .map(id -> id.getClaim("nickname"))
                        .orElse("anonymousNic"),
                "picture", Optional.ofNullable(authUser.getIdToken())
                        .map(id -> id.getClaim("picture"))
                        .orElse("anonymousPic")
        );

        return Jwts.builder()
                .subject(Optional.ofNullable(authUser.getIdToken())
                        .map(id -> (String) id.getClaim("sub"))
                        .orElse("anonymousSub"))
                .issuer(jwtProperties.iss())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .id(UUID.randomUUID().toString())
                .audience().add(SecurityLevel.LV0.getAccess()).and()
                .claims(claims)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String getJtiForFindRedisId(String token) {
        return validator.parseClaims(token).getId();
    }
    public String getSubjectForFindUserProviderId(String token) {
        return validator.parseClaims(token).getSubject();
    }
    public Instant getExpForRefreshTokenTtl(String token) {
        return validator.parseClaims(token).getExpiration().toInstant();
    }
}

