package com.app.pofolit_be.common.exception;

import lombok.Getter;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingRequestCookieException;

/**
 * 지정된 쿠키가 요청에서 없으면 호출됩니다.
 */
@Getter
public class RequestCookieException extends MissingRequestCookieException {

    private final ExCode exCode;

    /**
     * MissingRequestCookieException + ExCode
     *
     * @param exCode 쿠키관련 예외코드
     * @param cookieName 쿠키
     * @param parameter 누락된 쿠키의 파라미터
     * @param missingAfterConversion 변환 후 누락 여부
     */
    public RequestCookieException(ExCode exCode, String cookieName, MethodParameter parameter, boolean missingAfterConversion) {
        super(cookieName, parameter, missingAfterConversion);
        this.exCode = exCode;
    }

    @Override
    public String getMessage() {
        return this.exCode.getMessage();
    }
}