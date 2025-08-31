package com.app.pofolit_be.security.auth;

import com.app.pofolit_be.security.auth.jwt.TokenService;
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
import java.util.Map;

/**
 * OAuth2 인증 성공 처리 핸들러
 * JWT 토큰을 발급하고 쿠키로 만들고
 * ../auth/callback 경로로 쿠키를 담아 리다이렉트 시킵니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;

    @Value("${uri.auth.base}")
    private String baseUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        Map<String, String> tokens = tokenService.issueToken(user);
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        CookieUtil.addTokensToCookie(response, accessToken, refreshToken);
        getRedirectStrategy().sendRedirect(request, response, baseUri + "/auth/callback");
    }
}
