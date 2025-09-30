package com.app.pofolit_be.security.service;

import com.app.pofolit_be.security.dto.TokenProperties;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final StringRedisTemplate redisTemplate;
    private final TokenProperties tokenProperties;
    private final UserService userService;

    public Map<String, String> refreshAccessToken(String refreshToken) {
        Jwt decoded = decoder.decode(refreshToken);
        String refreshJti = decoded.getId();
        String subject = decoded.getSubject();

        String redisKey = subject + ":" + refreshJti;
        log.debug("최종 조회/삭제할 redisKey [{}]", redisKey);
        String existed = redisTemplate.opsForValue().getAndDelete(redisKey);

        if(existed == null) {
            revokeAllRefreshTokensForUser(subject);
            throw new BadJwtException("RefreshToken 만료 또는 재사용 감지하여 모든 sessions revoked.");
        }
        User user = userService.getUserBySubject(subject);
        String newAccessToken = issueAccessToken(user);
        String newRefreshToken = issueAndStoreRefreshToken(user);
        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    private String issueAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                .issuer(tokenProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(tokenProperties.getAccessTokenExp()))
                .subject(user.getSubject())
                .claim("role", user.getAccess() != null ? user.getAccess().name() : "LV0")
                .id(UUID.randomUUID().toString())
                .build();
        return encoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), accessClaims)
        ).getTokenValue();
    }

    public String issueAndStoreRefreshToken(User user) {
        Instant now = Instant.now();
        Instant refreshExp = now.plus(tokenProperties.getRefreshTokenExp());
        String refreshJti = UUID.randomUUID().toString();
        revokeAllRefreshTokensForUser(user.getSubject());
        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                .issuer(tokenProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(refreshExp)
                .subject(user.getSubject())
                .id(refreshJti).build();
        String refreshToken = encoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), refreshClaims)).getTokenValue();
        storeRefreshToken(user, refreshJti);
        return refreshToken;
    }

    private void storeRefreshToken(User user, String refreshJti) {
        String redisKey = user.getSubject() + ":" + refreshJti;
        Duration ttl = tokenProperties.getRefreshTokenExp();
        if(!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(redisKey, "뭐넣지", ttl);
        }
    }

    private void revokeAllRefreshTokensForUser(String subject) {
        String pattern = subject + ":*";
        var keysToDelete = new HashSet<String>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.keyCommands()
                    .scan(ScanOptions.scanOptions().match(pattern).count(1000).build())) {
                while (cursor.hasNext()) {
                    keysToDelete.add(new String(cursor.next()));
                }
            }
            return null;
        });

        if(!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }
}