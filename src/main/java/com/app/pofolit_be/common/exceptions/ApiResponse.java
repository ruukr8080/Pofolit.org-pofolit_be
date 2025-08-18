package com.app.pofolit_be.common.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * error response DTO
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 JSON에서 제외
public class ApiResponse {
    
    private LocalDateTime timestamp;    // 오류 발생 시간
    private int status;                 // HTTP 상태 코드
    private String error;              // 오류 타입/코드
    private String message;            // 사용자에게 보여줄 메시지
    private String path;               // 오류가 발생한 경로
    private Map<String, String> details; // 추가 상세 정보 (validation 오류 등)
}