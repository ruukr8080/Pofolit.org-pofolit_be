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
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
      try {
         UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
         User user = principal.getUser();
         Map<String, String> tokens = tokenService.issueToken(user);
         String accessToken = tokens.get("accessToken");
         String refreshToken = tokens.get("refreshToken");
         log.info("\n ACC  {}", accessToken);
         CookieUtil.addTokensToCookie(response, accessToken, refreshToken);
         getRedirectStrategy().sendRedirect(request, response, baseUri + "/auth/callback");
         log.info("\nNEW User: [{}]\n  ID [{}] \n  EMAIL [{}] \n  NICK [{}] \n  프사[{}]\n  providerId [{}]\n  registration [{}]\n  Token In Cookie: ['{}'] ",
                 user.getRole(), user.getId(), user.getEmail(), user.getNickname(), StringUtils.hasText(user.getProfileImageUrl()) ? "있음" : "없음", user.getProviderId(), user.getRegistrationId(), "ACCESS/REFRESH");
      } catch (Exception e) {
         log.error("OAuth2 인증 처리 중 예외 발생: {}", e.getMessage(), e);
         handleAuthenticationError(request, response, "auth_redirect", "인증 처리 중 서버 오류가 발생했습니다.");
      }
   }

   private void handleAuthenticationError(HttpServletRequest request, HttpServletResponse response, String
           errorCode, String errorMessage) throws IOException {
      String errorUrl = UriComponentsBuilder.fromUriString(baseUri)
              .queryParam("error", errorCode)
              .queryParam("message", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
              .build()
              .toUriString();
      getRedirectStrategy().sendRedirect(request, response, errorUrl);
   }
}
