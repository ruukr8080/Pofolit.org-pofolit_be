package com.app.pofolit_be.security.service;

import com.app.pofolit_be.security.dto.TokenPair;
import com.app.pofolit_be.security.dto.TokenProperties;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_KEY_PREFIX = "RT:";
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final StringRedisTemplate redisTemplate;
    private final TokenProperties tokenProperties;
    private final UserService userService;

    public TokenPair issueTokenPair(User user) {
        Instant now = Instant.now();
        Instant accessExp = now.plusSeconds(tokenProperties.getAccessTokenExp());
        Instant refreshExp = now.plusSeconds(tokenProperties.getRefreshTokenExp());

        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                .issuer(tokenProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(accessExp)
                .subject(user.getId().toString())
                .claim("role", user.getAccess() != null ? user.getAccess().name() : "LV0")
                .id(accessJti)
                .build();
        String accessToken = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(() -> "RS256").build(), accessClaims)
        ).getTokenValue();

        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                .issuer(tokenProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(refreshExp)
                .subject(user.getId().toString())
                .id(refreshJti)
                .build();
        String refreshToken = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(() -> "RS256").build(), refreshClaims)
        ).getTokenValue();

        String redisKey = REFRESH_KEY_PREFIX + user.getId() + ":" + refreshJti;  // 0.RT:1:3a551397-d469-442a-ac3b-210f3a5dc225
        long ttl = tokenProperties.getRefreshTokenExp();
        if(ttl > 0) {
            redisTemplate.opsForValue().set(redisKey, "0", java.time.Duration.ofSeconds(tokenProperties.getRefreshTokenExp()));
        }

        return new TokenPair(accessToken, refreshToken);
    }

    public TokenPair refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 디코딩. 여기서 서명/형식/만료시간 1차 검증.
        Jwt decoded = jwtDecoder.decode(refreshToken);
        String refreshJti = decoded.getId();
        String subject = decoded.getSubject();

        if(refreshJti == null || subject == null) {
            // 토큰에 jti나 subject 클레임이 없울 경우ㅜ.
            throw new BadJwtException("Refresh token is missing required claims (jti or sub).");
        }
        String redisKey = REFRESH_KEY_PREFIX + subject + ":" + refreshJti;
        String existed = redisTemplate.opsForValue().getAndDelete(redisKey);

        if(existed == null) {
            // Redis에 토큰이 없으면 이미 썼거나(재사용), 만료돼서 사라진 거.
            revokeAllRefreshTokensForUser(subject);
            throw new BadJwtException("Refresh token reuse detected or expired. All sessions are revoked.");
        }

        User user = userService.findById(Long.valueOf(subject))
                .orElseThrow(() -> new RuntimeException("UserNotFound: " + subject));
        return issueTokenPair(user);
    }

    // 조회한 사용자 id로 만든 RefreshToken Redis에서 다 삭제
    private void revokeAllRefreshTokensForUser(String userId) {
        Set<String> keys = redisTemplate.keys(REFRESH_KEY_PREFIX + userId + ":*");
        if(!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // RefreshToken.exp 반환.
    public long getRefreshTokenExpirySeconds() {
        return tokenProperties.getRefreshTokenExp();
    }
}