package com.app.pofolit_be.common;

import com.app.pofolit_be.common.exception.CustomException;
import com.app.pofolit_be.common.exception.ExCode;
import com.app.pofolit_be.common.exception.ExCodeException;
import com.app.pofolit_be.common.exception.RequestCookieException;
import com.app.pofolit_be.common.external.UriPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Arrays;

/**
 * 전역 예외 처리 및 공통 응답 래핑을 담당합니다.
 * <p>
 * 1. @ExceptionHandler: 프로젝트 전반의 예외를 잡아 ApiResult.error()로 응답.
 * 2. ResponseBodyAdvice: 컨트롤러의 정상 응답을 ApiResult.success()로 래핑.
 * </p>
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final UriPath excludePath;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * ExCodeException 인터페이스를 구현하는 모든 커스텀 예외를 중앙에서 처리한다.
     *
     * @param e ExCodeException을 구현한 예외
     * @param request 현재 웹 요청
     * @return ApiResult로 감싼 에러 응답
     */
    @ExceptionHandler({CustomException.class, RequestCookieException.class})
    public ResponseEntity<ApiResult<Void>> handleExCodeException(ExCodeException e, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        // 제외 경로에 해당하면 null을 반환하여 스프링 기본 핸들러에 위임 (Swagger 500 에러 방지)
        boolean isExcluded = Arrays.stream(excludePath.getException())
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));

        if(isExcluded) {
            log.debug(
                    "ApiResponseAdvice: 제외 경로({})에 대한 예외 처리를 건너뜁니다.",
                    request.getDescription(false));
            return null;
        }

        ExCode exCode = e.getExCode();
        log.warn(
                "\n[{}] 에러코드,메세지,경로: \n[{}]\n[{}]\n[{}]",
                e.getClass().getSimpleName(),
                exCode.name(),
                exCode.getMessage(),
                request.getDescription(false));

        ApiResult<Void> result =
                ApiResult.error(exCode.getStatus().value(), exCode.name(), exCode.getMessage());

        return new ResponseEntity<>(result, exCode.getStatus());
    }

    // --- ResponseBodyAdvice: 성공 응답 래핑 ---

    @Override
    public boolean supports(
            MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 에러 응답이나 이미 ApiResult로 감싸진 응답은 건너뛴다.
        return !returnType.getParameterType().equals(ApiResult.class)
                && !returnType
                .getGenericParameterType()
                .getTypeName()
                .equals("org.springframework.http.ResponseEntity<com.app.pofolit_be.common.ApiResult<java.lang.Void>>");
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        String path = ((ServletServerHttpRequest) request).getServletRequest().getRequestURI();

        // 제외 경로에 해당하면 원본 그대로 반환
        boolean isExcluded = Arrays.stream(excludePath.getException())
            .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isExcluded) {
            return body;
        }

        return ApiResult.success("요청에 성공했습니다.", body);
    }
}