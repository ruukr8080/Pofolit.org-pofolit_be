package com.app.pofolit_be.common.exceptions;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT, OAuth2, business exception
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * JWT 관련 예외 처리
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse> handleJwtException(JwtException ex, WebRequest request) {
        log.warn("JWT 예외 발생: {}", ex.getMessage());
        
        ApiResponse error = ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("JWT_ERROR")
                .message("JWT 토큰이 유효하지 않습니다")
                .path(getPath(request))
                .build();
                
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * OAuth2 인증 예외 처리
     */
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleOAuth2Exception(OAuth2AuthenticationException ex, WebRequest request) {
        log.warn("OAuth2 인증 예외 발생: {}", ex.getMessage());
        
        ApiResponse error = ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("OAUTH2_ERROR")
                .message("소셜 로그인 처리 중 오류가 발생했습니다")
                .path(getPath(request))
                .build();
                
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }


    /**
     * 인증 실패 시 에러 페이지로 리다이렉트
     * @param response      HttpServletResponse
     * @param errorCode     에러 코드
     * @param errorMessage  에러 메시지
     */
//    private void handleAuthenticationError(HttpServletResponse response, String errorCode, String errorMessage) throws IOException {
//        String errorUrl = UriComponentsBuilder.fromUriString(baseUri)
//                .queryParam("error", errorCode)
//                .queryParam("message", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
//                .build()
//                .toUriString();
//        getRedirectStrategy().sendRedirect(null, response, errorUrl);
//    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        log.warn("인증 실패: {}", ex.getMessage());
        
        ApiResponse error = ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("AUTHENTICATION_FAILED")
                .message("인증에 실패했습니다")
                .path(getPath(request))
                .build();
                
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * 권한 부족 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("접근 권한 없음: {}", ex.getMessage());
        
        ApiResponse error = ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("ACCESS_DENIED")
                .message("접근 권한이 없습니다")
                .path(getPath(request))
                .build();
                
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("검증 오류: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ApiResponse error = ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_FAILED")
                .message("입력값 검증에 실패했습니다")
                .path(getPath(request))
                .details(errors)
                .build();
                
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 일반적인 RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Runtime 예외 발생: {}", ex.getMessage(), ex);
        
        ApiResponse error = ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("서버 내부 오류가 발생했습니다")
                .path(getPath(request))
                .build();
                
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 모든 예외의 최종 처리기
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("예상치 못한 예외 발생: {}", ex.getMessage(), ex);
        
        ApiResponse error = ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("UNEXPECTED_ERROR")
                .message("예상치 못한 오류가 발생했습니다")
                .path(getPath(request))
                .build();
                
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 요청 경로 추출 유틸리티 메서드
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}