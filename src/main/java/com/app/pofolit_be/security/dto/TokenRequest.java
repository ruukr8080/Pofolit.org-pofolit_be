package com.app.pofolit_be.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TokenRequest
 *
 * <p>클라이언트로부터 리프레시 토큰 재발급 요청 시
 * HTTP 바디에 담긴 토큰을 받는 DTO입니다.
 * </p>
 *
 * @param token 리프레시 토큰
 */
public record TokenRequest(
        @JsonProperty("token")
        String token
) {
}
