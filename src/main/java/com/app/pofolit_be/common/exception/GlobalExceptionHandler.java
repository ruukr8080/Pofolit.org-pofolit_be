package com.app.pofolit_be.common.exception;

import com.app.pofolit_be.common.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * 전역 예외 핸들러입니다.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * 직접 정의한 {@link CustomException}을 처리합니다.
     *
     * @param e CustomException.instance
     * @param request request
     * @return ApiResult로 감싸진 에러 응답
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResult<Void>> handleRuntimeException(CustomException e, WebRequest request) {
        ExCode exCode = e.getExCode();
        log.warn("\n[CustomException] 에러코드,메세지,경로: \n[{}]\n[{}]\n[{}]",
                exCode.name(), exCode.getMessage(), request.getDescription(false));
        ApiResult<Void> result = ApiResult.error(
                exCode.getStatus().value(),
                exCode.name(),
                exCode.getMessage()
        );
        return new ResponseEntity<>(result,
                exCode.getStatus()
        );
    }

    /**
     * 지정된 쿠키가 누락되면 발생합니다ㅏ. {@link RequestCookieException}
     *
     * @param e RequestCookieException.instance
     * @param request request
     * @return ApiResult로 감싸진 에러 응답
     */
    @ExceptionHandler(RequestCookieException.class)
    public ResponseEntity<ApiResult<Void>> handleRequestCookieException(RequestCookieException e, WebRequest request) {
        ExCode exCode = e.getExCode();
        log.warn("\n[RequestCookieException] 에러코드,메세지,경로: \n[{}]\n[{}]\n[{}]",
                exCode.name(), exCode.getMessage(), request.getDescription(false));
        ApiResult<Void> result = ApiResult.error(
                exCode.getStatus().value(),
                exCode.name(),
                exCode.getMessage()
        );
        return new ResponseEntity<>(result,
                exCode.getStatus()
        );
    }
}