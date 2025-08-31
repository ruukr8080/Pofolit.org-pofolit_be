package com.app.pofolit_be.security.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * JWT 설정 값을 바인딩합니다.
 * <p>
 *     {@code application.yml} 파일에서
 *     {@code jwt} 접두사 밑에 설정 된 파라미터들입니다.
 * </p>
 *
 * @param secret 서명용 secret key ("arg":"HS512")
 * @param authorityKey Spring_Security에서 인가 정보를 저장할 떄 쓰게 될 key
 * @param accessTokenHeader HTTP 헤더 객체에 박을 이름 : "Authorization"
 * @param refreshTokenCookie (Cookie)refreshToken 이름 : "refreshToken"
 * @param accessTokenExpOfSecond accessToken 만료기간 (초 단위)
 * @param refreshTokenExpOfDay refreshToken 만료기간 (일 단위)
 */
@ConfigurationProperties(prefix = "spring.jwt")
public record TokenProperties(
        String secret,
        String authorityKey,
        String accessTokenHeader,
        String refreshTokenCookie,
        long accessTokenExpOfSecond,
        int refreshTokenExpOfDay
) { }