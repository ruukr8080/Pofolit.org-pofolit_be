package com.app.pofolit_be.security.token;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * OAuth2 인증 요청 정보를 세션 대신 쿠키에 저장하는 클래스.
 * <p>
 * STATELESS 환경에서 OAuth2를 안전하게 사용하기 위함.
 * 인증 요청 정보를 직렬화하여 쿠키에 저장하고, 콜백 시 쿠키에서 역직렬화하여 사용.
 * 사용이 끝난 쿠키는 즉시 삭제하여 보안을 유지.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CookieRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest>
{

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "pre";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";

    private final CookieUtil cookieUtil;

    /**
     * 로그인 끝나고 콜백 돌아왔을 때, 쿠키에서 인증 요청 정보를 다시 읽습니ㅏㄷ.
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        log.info(" loadAuthorizationRequest 쿠키: {}", cookieUtil.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        );
        return cookieUtil
                .getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> cookieUtil.reverseSerialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);

    }

    /**
     * 로그인 시작할 때, 인증 요청 정보를 쿠키에 저장합니다.
     */
    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info("요청에 쿠키있음??? : \n{}", authorizationRequest.getAuthorizationUri());
        if(authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        cookieUtil.addCookie(
                response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                cookieUtil.serialize(authorizationRequest));
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        log.info("REDIRECT_URI_PARAM_COOKIE_NAME ?? : \n{}", redirectUriAfterLogin);
        if(StringUtils.isNotBlank(redirectUriAfterLogin)) {
            cookieUtil.addCookie(
                    response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin);
            log.info("StringUtils.isNotBlank(redirectUriAfterLogin : \n{}", redirectUriAfterLogin);
        }

    }

    /**
     * 인증정보를 로드했으면 flush 로 마무리합니다.
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
        log.info(" removeAuthorizationRequest 쿠키: \n{}", authorizationRequest);
        this.removeAuthorizationRequestCookies(request, response);
        return authorizationRequest;
    }

    /**
     * 로그인 성공/실패 후에 AuthSuccessHandler에서 호출해서 썼던 임시 쿠키를 flush
     */
    public void removeAuthorizationRequestCookies(
            HttpServletRequest request, HttpServletResponse response) {
        cookieUtil.expireCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        log.info("OAUTH2_AUTHORIZATIO  쿠키: {}", response);
        log.info("REDIRECT_URI_PARAM  쿠키: {}", request);

        cookieUtil.expireCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
