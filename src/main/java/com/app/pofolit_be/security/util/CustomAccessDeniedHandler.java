package com.app.pofolit_be.security.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication(); // AuthenticationContext 에서 인증 정보 추출하고 로깅
        if(auth != null) {
            log.warn("접근 거부된 자: {} 접근 경로: {}",
                    auth.getName(), request.getRequestURI()); // (auth.getName() == User ID) 접근이 거부된 사용자 로깅.
        }
        // OAuth 2.1 보안 권장 사항에 따라 권한 부족 오류는 403 Forbidden을 반환.
        String message = String.format("{\"error\": \"forbidden\", \"message\": \"Access Denied. 리소스에 접근할 권한이 없습니다.\", \"detail\": \"%s\"}",
                accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : "Insufficient privileges.");
        response.getWriter().write(message);
    }
}