package com.app.pofolit_be.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Runtime중 발생한 Exception 입니다.
 * ErrorCode에 따른 메시지를 반환합니다.
 */
@Getter
@RequiredArgsConstructor
public class ExceptionProvider extends RuntimeException implements ExCodeException {

    private final ExCode exCode;

    @Override
    public String getMessage() {
        return this.exCode.getMessage();
    }

}
