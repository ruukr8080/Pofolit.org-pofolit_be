package com.app.pofolit_be.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 공통 응답 형싣입니다.
 *
 * <p>
 * 모든 API 응답은 이 클래스를 사용하여 일관된 형식으로 클라이언트에게 전달됩니다..
 * `JsonPropertyOrder` 로 순서 지정했습니다.
 * 요청 경로는 `ApiResultAdvice`에서 넣어줍니다.
 * </p>
 *
 * @param <T> 응답 데이터의 타입
 */
@Schema(name = "ApiResult", description = "API 공통 응답 ")
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"timestamp", "status", "message", "data"})
public class ApiResult<T> {

    @Schema(description = "응답 시간", example = "2025-09-03T10:00:00")
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "HTTP State", example = "200,400 ...")
    private final int status;

    @Schema(description = "메시지", example = "요청에 성공했습니다.")
    private final String message;

    @Schema(description = "응답 data")
    private final T data;

    // 성공
    private ApiResult(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // 실패
    private ApiResult(int status, String error, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    /**
     * 성공 응답을 생성합니다.
     *
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @return ApiResult 인스턴스
     * @param <T> 데이터 타입
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(200, message, data);
    }

    public static ApiResult<Void> success(String message) {
        return new ApiResult<>(200, message, null);
    }

    public static ApiResult<Void> error(int status,
                                        String error,
                                        String message
    ) {
        return new ApiResult<>(status, error, message);
    }
}
