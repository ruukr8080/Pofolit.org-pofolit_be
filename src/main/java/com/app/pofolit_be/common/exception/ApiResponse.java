package com.app.pofolit_be.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API 요청에 대한 표준 응답 형식을 정의합니다.
 *
 * <p>
 *     모든 API 응답은 이 클래스를 사용하여 일관된 형식으로 클라이언트에게 전달됩니다..
 *     예외 발생 시 클라이언트가 에러 상황을 명확히 인지하고 처리할 수 있도록 하게 해야합니다.
 * </p>
 */
@Schema(description = "API 공통 응답 형식")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
// null 값은 JSON에서 제외
public class ApiResponse {

    @Schema(description = "응답 생성 시간", example = "2023-11-27T10:00:00")
    private final LocalDateTime timestamp;

    @Schema(description = "HTTP 상태 코드", example = "404")
    private final int status;

    @Schema(description = "HTTP 상태 메시지 또는 커스텀 에러 코드", example = "NOT_FOUND")
    private final String error;

    @Schema(description = "클라이언트에게 보여줄 구체적인 응답 메시지", example = "해당 리소스를 찾을 수 없습니다.")
    private final String message;

    @Schema(description = "요청이 발생한 API 경로", example = "/api/v1/users/123")
    private final String path;

    @Schema(description = "유효성 검사(Validation) 실패 시 상세 내역. (key: 필드명, value: 에러 메시지)")
    private Map<String, String> details;
}