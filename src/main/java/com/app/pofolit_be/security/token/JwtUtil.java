package com.app.pofolit_be.security.token;

import com.app.pofolit_be.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@PropertySource("classpath:application-mydev.yml")
@RequiredArgsConstructor
public class JwtUtil {

    private static final MacAlgorithm SIG_ALG = Jwts.SIG.HS256;
    private static final String ISSUER = "Pofolit";
    private static final long ACCESS_TOKEN_TTL = 600L;
    private static final long REFRESH_TOKEN_TTL = 259200L;
    private final JwtProperties jwtProperties;
    private final CookieUtil cookieUtil;
    private SecretKey secretKey;

    /**
     * `Signature` 암호화
     * 스프링 켜지면 시크릿 키 초기화합니다.
     */
    @PostConstruct
    public void init() {
        this.secretKey = getKeyFromBase64EncodedKey(jwtProperties.secret());
        //        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64로 인코딩된 시크릿 키를 디코딩하여 SecretKey 객체를 반환합니다.
     *
     * @param base64EncodedSecretKey Base64 인코딩된 키 문자열
     * @return SecretKey 객체
     */
    private SecretKey getKeyFromBase64EncodedKey(String base64EncodedSecretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 공통 로직을 사용하여 JWT 토큰을 생성합니다.
     *
     * @param sub subject
     * @return Token
     */
    private String generateToken(String sub, long ttl) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttl);

        return Jwts.builder()
                .subject(sub)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey, SIG_ALG)
                .compact();
    }

    /**
     * Access Token을 생성합니다.
     *
     * @param sub 토큰의 주체가 될 사용자 ID
     * @return 생성된 액세스 토큰
     */
    public String generateAccessToken(String sub) {
        return generateToken(sub, ACCESS_TOKEN_TTL);
    }

    /**
     * Refresh Token을 생성하고 Redis에 저장합니다.
     *
     * @param sub 토큰의 주체가 될 사용자 ID
     * @return 생성된 리프레시 토큰
     */
    public String generateRefreshToken(String sub) {

        return generateToken(sub, REFRESH_TOKEN_TTL);
    }

    /**
     * 토큰의 (만료 , 서명) 검증.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.toInstant().isAfter(Instant.now());
        } catch (JwtException |
                 IllegalArgumentException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 클레임 추출
     *
     * @param token 클레임을 추출할 JWT 토큰
     * @return Payload
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)       // 서명 검증 키
                .build()
                .parseSignedClaims(token)    // 서명된 JWT 파싱
                .getPayload();               // Claims 반환
    }

    /**
     * `JwtToken`에서 `sub` 추출
     *
     * @param token 유저 ID를 추출할 JWT 토큰
     * @return 추출된 유저 ID
     */
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }
    /**
     * 요청에서 인증객체 추출.
     * 1. Authorization 헤더에서 먼저 시도합니다.
     * 2. 헤더에 없으면 'Cookie'에서 찾습니다.{refreshToken}
     *
     * @param request HttpServletRequest
     * @return 추출된 토큰 문자열, 없으면 null
     */
    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.info("요청헤더에 Authorization\n[{}]", bearerToken);
        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        Optional<Cookie> accessTokenCookie = cookieUtil.getCookie(request, "pre");
        String accessToken = accessTokenCookie.map(Cookie::getValue).orElse(null);

        if (accessToken != null && isValid(accessToken)) {
            log.info("AccessToken 유효함");
            return accessToken;
        } else {
            log.info("AccessToken이 없거나 만료됨, RefreshToken 확인로직으로.");
            Optional<Cookie> refreshTokenCookie = cookieUtil.getCookie(request, "refreshToken");
            return refreshTokenCookie.map(Cookie::getValue).orElse(null);
        }
    }


}