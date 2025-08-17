package com.app.pofolit_be.common.utils;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * API 응답을 일관된 형태로 처리하는 유틸리티 클래스
 * 성공/실패 응답을 표준화된 형태로 반환함
 */
public class ResponseUtil {

    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(data, "요청이 성공적으로 처리되었습니다");
    }

    /**
     * 성공 응답 생성 (데이터 + 메시지)
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 성공 응답 생성 (메시지만)
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 생성 성공 응답 (201 Created)
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 실패 응답 생성 (400 Bad Request)
     */
    public static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 인증 실패 응답 (401 Unauthorized)
     */
    public static ResponseEntity<ApiResponse<Void>> unauthorized(String message) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 권한 부족 응답 (403 Forbidden)
     */
    public static ResponseEntity<ApiResponse<Void>> forbidden(String message) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 리소스 없음 응답 (404 Not Found)
     */
    public static ResponseEntity<ApiResponse<Void>> notFound(String message) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 서버 오류 응답 (500 Internal Server Error)
     */
    public static ResponseEntity<ApiResponse<Void>> internalError(String message) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 표준 API 응답 DTO
     */
    @Getter
    @Builder
    public static class ApiResponse<T> {
        private boolean success;      // 성공 여부
        private String message;       // 응답 메시지
        private T data;              // 응답 데이터
        private LocalDateTime timestamp; // 응답 시간
    }
}