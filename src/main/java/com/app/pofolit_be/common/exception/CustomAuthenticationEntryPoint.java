package com.app.pofolit_be.common.exception;

import com.app.pofolit_be.common.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.warn("인증 실패. Security Filter Chain에서 예외 발생. 요청 URI: {}", request.getRequestURI());
        log.warn("발생한 예외 클래스: {}", authException.getClass().getName());
        log.warn("예외 메시지: {}", authException.getMessage());

        ExCode exCode;
        Throwable cause = authException.getCause();

        if (cause != null) {
            log.warn("예외의 원인 클래스: {}", cause.getClass().getName());
            log.warn("원인 메시지: {}", cause.getMessage());
            if (cause instanceof ExpiredJwtException) {
                exCode = ExCode.EXPIRED_TOKEN;
            } else {
                exCode = ExCode.ERROR_AUTH;
            }
        } else {
            // 원인이 없는 경우, authException 자체를 기반으로 판단
            exCode = ExCode.ERROR_AUTH;
        }

        // 3. 클라이언트에게 보낼 표준 에러 응답 생성
        ApiResult<Void> apiResult = ApiResult.error(
                exCode.getStatus().value(),
                exCode.name(),
                exCode.getMessage()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        try (OutputStream out = response.getOutputStream()) {
            objectMapper.writeValue(out, apiResult);
        }
    }
}