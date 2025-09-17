package com.app.pofolit_be.common.exception;

/**
 * ExCode를 반환하는 모든 예외가 구현해야 하는 인터페이스.
 * <p>
 * ApiResponseAdvice에서 이 인터페이스를 사용하여 예외를 중앙에서 처리한다.
 * </p>
 */
public interface ExCodeException {
  ExCode getExCode();
}