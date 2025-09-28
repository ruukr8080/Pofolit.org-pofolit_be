package com.app.pofolit_be.common.exception;

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

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String BEARER_REALM = "Bearer realm=\"pofolit.api\", error=\"invalid_token\""; // Bearer 인증 체계

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 접근 토큰이 만료, 위변조, 아예 없는 경우 401 반환
        response.setHeader("WWW-Authenticate", BEARER_REALM); // 리소스 서버접근할땐 반드시 WWW-Authenticate 응답 헤더 필드가 있어여함
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String message = String.format("{\"error\": \"unauthorized\", \"message\": \"%s\"}",
                authException.getMessage() != null ? authException.getMessage() : "인증 가능한 토큰이 헤더에 없습니다.");

        response.getWriter().write(message);
        log.warn("인증 안됨: URI={}, Error={}", request.getRequestURI(), authException.getMessage());
    }
}
