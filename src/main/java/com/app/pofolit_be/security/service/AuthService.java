package com.app.pofolit_be.security.service;

import com.app.pofolit_be.security.SecurityLevel;
import com.app.pofolit_be.security.authentication.AuthenticatedUser;
import com.app.pofolit_be.security.config.JwtProperties;
import com.app.pofolit_be.security.token.CookieUtil;
import com.app.pofolit_be.security.token.RedisUtil;
import com.app.pofolit_be.security.token.TokenUtil;
import com.app.pofolit_be.security.token.TokenValidator;
import com.app.pofolit_be.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final TokenUtil tokenUtil;
    private final TokenValidator validator;
    private final RedisUtil redisUtil;
    private final CookieUtil cookieUtil;
    private final UserService userService;
    private final JwtProperties jwtProperties;
    public SecurityLevel securityLevel;

    public void issuePreToken(HttpServletResponse response) {
        String preToken = tokenUtil.generatePreToken();
        ResponseCookie cookie = ResponseCookie.from("pre", preToken)
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtProperties.pTtl())
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public Map<String, ResponseCookie> issuePairTokens(AuthenticatedUser authUser) {
        // IdToken만 들어옴.

        authUser.getIdToken().getClaims();
        String accessToken = tokenUtil.generateAccessToken(authUser, securityLevel);
        String refreshToken = tokenUtil.generateRefreshToken(authUser, securityLevel);
        Map<String, ResponseCookie> cookies = new HashMap<>();

        cookies.put("accessToken", cookieUtil.createCookie("accessToken", accessToken, false, 60 * 60));
        cookies.put("refreshToken", cookieUtil.createCookie("refreshToken", refreshToken, true, 604800));

        return cookies;
    }

    public Authentication createAuthentication(String token) {
        Claims claims = validator.parseClaims(token);
        String sub = claims.getSubject();
        String role = claims.get("aud", String.class);
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        return new UsernamePasswordAuthenticationToken(sub, null, authorities);
    }

    public void logout(String sub, String jti, HttpServletResponse response) {
        redisUtil.deleteRT(sub, jti);
        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", deleteRefreshCookie.toString());
    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        int tempDay = 10 * 24 * 60 * 60;
        try {
            validator.validateToken(refreshToken);

            String subFromRequest = tokenUtil.getSubjectForFindUserProviderId(refreshToken);
            String jtiFromRequest = tokenUtil.getJtiForFindRedisId(refreshToken);
            String redisRefToken = redisUtil.getRT(subFromRequest, jtiFromRequest);
            if(redisRefToken == null) {
                throw new RuntimeException("accessToken 재발급하려는데 레디스에 없습니다.만료돼서 자동삭제 됐을수도 있습니다.");
            }
            // TODO: 조건부 갱신 구현하기.
            //  - 엑세스토큰 재발급 시 리프레시 토큰 유효기간(임계값)이 10일 미만정도 ?
            //  [ture] 리프레시토큰 기존꺼 지우고 새로갱신.
            //  : [false] 리프레시토큰은 유지. or 리프레시토큰 ttl클레임만 초기화(의미없을듯). or
            //        AuthenticatedUser test = (AuthenticatedUser) createAuthentication(redisRefToken);
            AuthenticatedUser authUser = userService.getAuthenticatedUserFrom(subFromRequest);
            String newAccessToken = tokenUtil.generateAccessToken(authUser, securityLevel);
            Instant refExpInstant = tokenUtil.getExpForRefreshTokenTtl(refreshToken);
            Instant now = Instant.now();
            Duration remainingDuration = Duration.between(now, refExpInstant);

            Map<String, String> tokens = new HashMap<>();

            if(remainingDuration.toDays() < tempDay) {
                String newRefreshToken = tokenUtil.generateRefreshToken(authUser, securityLevel);
                redisUtil.deleteRT(subFromRequest, jtiFromRequest);
                redisUtil.setRT(subFromRequest, jtiFromRequest, newRefreshToken, jwtProperties.rTtl());
                tokens.put("accessToken", newAccessToken);
                tokens.put("refreshToken", newRefreshToken);
            } else {
                tokens.put("accessToken", newAccessToken);
                tokens.put("refreshToken", refreshToken); // 기존꺼유지
                return tokens;
            }
            return tokens;
        } catch (ExpiredJwtException e) {
            log.warn("Refresh Token 만료: {}", e.getMessage());
            throw new RuntimeException("refreshToken이 만료되었습니다.");
        } catch (SignatureException e) {
            log.warn("서명이 유효하지 않거나 위조되었습니다.: {}", e.getMessage());
            throw new RuntimeException("refreshToken 위조");
        } catch (MalformedJwtException |
                 UnsupportedJwtException |
                 IllegalArgumentException e) {
            log.error("지원하지 않는 형식입니다.: {}", e.getMessage());
            throw new RuntimeException("refreshToken 형식이 올바르지 않습니다.");
        }

    }
}