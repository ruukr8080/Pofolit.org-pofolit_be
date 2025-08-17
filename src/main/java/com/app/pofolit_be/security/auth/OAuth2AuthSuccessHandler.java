package com.app.pofolit_be.security.auth;

import com.app.pofolit_be.security.auth.jwt.JwtUtil;
import com.app.pofolit_be.user.dto.CustomUserDetails;
import com.app.pofolit_be.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 로그인 성공 시 JWT 토큰 발급하고 프론트엔드로 리다이렉트하는 핸들러
 * Google, Kakao 로그인 성공 후 JWT 토큰 생성해서 클라이언트에게 전달함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/auth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        try {
            // OAuth2 인증된 사용자 정보 추출
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // JWT 토큰 생성
            String jwtToken = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getKey()
            );

            log.info("OAuth2 로그인 성공 - JWT 토큰 발급: userId={}, email={}, provider={}",
                    user.getId(), user.getEmail(), user.getRegistrationId());

            // 프론트엔드로 리다이렉트 URL 생성 (토큰 포함)
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", jwtToken)
                    .queryParam("user", URLEncoder.encode(user.getNickname(), StandardCharsets.UTF_8))
                    .build()
                    .toUriString();

            log.debug("OAuth2 성공 리다이렉트 URL: {}", targetUrl);

            // 리다이렉트 실행
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생: {}", e.getMessage(), e);
            
            // 오류 발생 시 에러 페이지로 리다이렉트
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "authentication_failed")
                    .queryParam("message", URLEncoder.encode("인증 처리 중 오류가 발생했습니다", StandardCharsets.UTF_8))
                    .build()
                    .toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    /**
     * 리다이렉트 URL 결정 로직
     * 나중에 다양한 클라이언트 지원 시 확장 가능 ㅇㅇ
     */
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        // 요청에서 redirect_uri 파라미터 확인
        String targetUrl = request.getParameter("redirect_uri");
        
        if (targetUrl != null && isAuthorizedRedirectUri(targetUrl)) {
            return targetUrl;
        }
        
        return redirectUri; // 기본 리다이렉트 URI 사용
    }

    /**
     * 허용된 리다이렉트 URI인지 검증
     * 보안상 허용된 도메인만 리다이렉트 허용
     */
    private boolean isAuthorizedRedirectUri(String uri) {
        // 로컬 개발 환경과 프로덕션 환경 URL 허용
        return uri.startsWith("http://localhost:3000") ||
               uri.startsWith("https://pofolit.org") ||
               uri.startsWith("https://www.pofolit.org");
    }
}