package com.app.pofolit_be.security.token;

import com.app.pofolit_be.security.SecurityLevel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenValidator {

    public PublicKey publicKey;

    public Claims parseClaims(String token) {
        return Jwts.parser() // Jwts.parser : 유효성 검사 자동
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void validateToken(String token) {
        Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);
    }

    public boolean validateSecurityLv(String token, SecurityLevel slv) {
        try {
            validateToken(token);
        } catch (JwtException e) {
            log.warn("접근 권한이 없는 토큰입니다.: \n{}", e.getMessage());
            return false;
        }
        Claims claims = parseClaims(token);
        Set<String> audience = Optional.ofNullable(claims.getAudience())
                .orElse(Collections.emptySet());
        return audience.contains(slv.getAccess());
    }
}
