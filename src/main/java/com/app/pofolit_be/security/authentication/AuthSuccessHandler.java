package com.app.pofolit_be.security.authentication;

import com.app.pofolit_be.security.authentication.OIDCUser;
import com.app.pofolit_be.security.token.CookieUtil;
import com.app.pofolit_be.security.token.JwtUtil;
import com.app.pofolit_be.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 인증 성공 후 최종 처리를 담당하는 핸들러입니다.
 * 이 핸들러는 오직 토큰을 쿠키에 담고 지정된 URL로 리다이렉트하는 역할만 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
//    private final CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    // application.yml에 설정된 프론트엔드 리다이렉트 URL
    @Value("${uri.auth.base}")
    private String targetUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 1. 인증 정보에서 사용자 정보 추출
        OIDCUser principal = (OIDCUser) authentication.getPrincipal();
        User user = principal.getUser();
        String sub = user.getProviderId();

        // 2. AccessToken과 RefreshToken 생성
        String accessToken = jwtUtil.generateAccessToken(sub);
        String refreshToken = jwtUtil.generateRefreshToken(sub);
        // (참고: RefreshToken 생성 시 Redis 저장 로직은 JwtUtil 또는 그 내부 서비스에서 처리됩니다.)

        // 3. 토큰을 쿠키에 담아 전달
        // RefreshToken은 HttpOnly:`true`
        cookieUtil.addCookie(response, "pre", accessToken);
        cookieUtil.addCookie(response,"refreshToken", refreshToken);

        // 4. 설정된 프론트엔드 URL로 리다이렉트
        log.info("[{}]", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }

    /**
     * 인증 과정에서 사용한 임시 쿠키(JSESSION)를 삭제합니다.
     */
//    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
//        super.clearAuthenticationAttributes(request);
//        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
//    }
}
