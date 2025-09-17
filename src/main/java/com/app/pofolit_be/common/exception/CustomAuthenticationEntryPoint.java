package com.app.pofolit_be.common.exception;

import com.app.pofolit_be.common.ApiResult;
import com.app.pofolit_be.common.external.UriPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final UriPath excludePath;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @PostConstruct
    public void init() {
        // 이 클래스는 이제 @PostConstruct에서 미리 작업할 필요가 없음.
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        // ExcludePathProperties에 정의된 경로인지 확인
        String requestURI = request.getRequestURI();
        boolean isExcluded = Arrays.stream(excludePath.getException())
            .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
        if(isExcluded) {
            log.info("인증 예외가 발생했지만, 제외 경로에 해당하여 처리를 건너뜁니다: {}", requestURI);
            return; // 예외 건너뛰고 반환
        }
//        log.warn("인증 실패. Security Filter Chain에서 예외 발생. 요청 URI: {}", request.getRequestURI());
//        log.warn("발생한 예외 클래스: {}", authException.getClass().getName());
//        log.warn("예외 메시지: {}", authException.getMessage());

        Throwable cause = authException.getCause();

        // 원인(cause)이 ExpiredJwtException이면 EXPIRED_TOKEN, 아니면 기본적으로 ERROR_AUTH 사용
        ExCode exCode = (cause instanceof ExpiredJwtException) ? ExCode.EXPIRED_TOKEN : ExCode.ERROR_AUTH;

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