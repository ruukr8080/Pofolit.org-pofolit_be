package com.app.pofolit_be.common.exception;

import com.app.pofolit_be.common.ApiResponseAdvice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 구현 된 모든 {@code Custom~~~Exception}예외 클래스는
 * {@link ExCode}를 참좋하고
 * {@link ApiResponseAdvice}를 통해 클라이언트에게 전달합니다.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(name = "예외 코드와 메세지")
public enum ExCode {

    /**
     * 프론트 ExCode. `./type/http.ts`에 있습니다.
     * <p>
     * EXCEPTION_CONSTRUCTOR = "EXCEPTION_CONSTRUCTOR", // 프론트에서 메세지 등 커스터마이징
     * ERROR_SERVER = "ERROR_SERVER", // 백엔드잘못
     * INVALID_TYPE = "INVALID_TYPE", // type
     * INVALID_VALUE = "INVALID_VALUE", // value
     * INVALID_TOKEN = "INVALID_TOKEN", // 리프레시 토큰이 유효하지 않음
     * DUPLICATE_SOCIAL_EMAIL = "DUPLICATE_SOCIAL_EMAIL", // 이미 사용 중인 소셜 이메일
     * DUPLICATE_EMAIL = "DUPLICATE_EMAIL" // 이미 가입 한 사용자입니다.
     * DUPLICATE_USER = "DUPLICATE_USER", // 이미 사용자가 존재
     * DUPLICATE_NICKNAME = "DUPLICATE_NICKNAME", // 이미 존재하는 닉네임
     * EXPIRED_TOKEN = "EXPIRED_TOKEN", // 토큰 유효기간 만료
     * NOT_FOUND_USER = "NOT_FOUND_USER", // 유저를 찾을 수 없음
     */
    ERROR_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 예기치 못한 에러가 발생했습니다"),
    INVALID_TYPE(HttpStatus.BAD_REQUEST, "요청한 파라미터의 타입이 불일치합니다."),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, "요청한 파라미터의 값이 불일치합니다."),
    INVALID_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 관련 에러 발생"),
    DUPLICATE_SOCIAL_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입 한 사용자입니다."),
    DUPLICATE_ACCOUNT(HttpStatus.CONFLICT, "이미 가입  된 계정 있습니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임 입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 유효기간이 만료되었습니다"),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "요청한 사용자를 찾을 수 없습니다"),

    NULL_(HttpStatus.BAD_REQUEST, "Null , Undefined 입니다."),
    ERROR_AUTH(HttpStatus.INTERNAL_SERVER_ERROR, "인증 관련 에러 발생"),

    NOT_FOUND_TOKEN_IN_HEADER(HttpStatus.NOT_FOUND, "요청 헤더에 토큰이 없습니다"),
    NOT_FOUND_TOKEN_IN_COOKIE(HttpStatus.NOT_FOUND, "인증 가능한 쿠키가 없습니다"),
    INVALID_OAUTH_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "OAuth 리프레시 토큰이 유효하지않습니다."),
    NOT_FOUND_JWT_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "요청한 JWT 리프레시 토큰을 찾을 수 없습니다"),
    NOT_FOUND_OAUTH_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "요청한 Oauth 리프레시 토큰을 찾을 수 없습니다"),

    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis Server에서 오류가 발생했습니다"),
    FAILED_DELETE_REFRESH_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 에서 리프레시 토큰을 삭제하는 중 오류가 발생했습니다"),
    FAILED_FIND_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Redis 에서 리프레시 토큰을 찾지 못했습니다"),
    ;

    private final HttpStatus status;
    private final String message;
}
