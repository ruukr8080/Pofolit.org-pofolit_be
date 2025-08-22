package com.app.pofolit_be.security.auth;

import com.app.pofolit_be.security.auth.jwt.JwtUtil;
import com.app.pofolit_be.user.dto.UserPrincipal;
import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 인증 서명 -> redirect or input additional userinfo -> save ->
 * Google, Kakao 로그인 성공 시 JWT 토큰을 생성해 클라이언트에게 전달한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

   private final JwtUtil jwtUtil;
   private final UserRepository userRepository;

   @Value("${uri.auth.base}")
   private String baseUri;
   @Value("${uri.auth.signup}")
   private String signupUri;

   @Override
   public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
      // 1. user info from Principal
      User user = null;
      if(authentication.getPrincipal() instanceof UserPrincipal) {
         UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
         user = principal.getUser();
      } else if(authentication.getPrincipal() instanceof OAuth2User) {
         // OAuth2UserService (디버깅용)
         OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
         log.warn("oauth2User type {} ", oauth2User.getClass().getName());
         handleAuthenticationError(request, response, "auth_error", "FAIL -> input additional info");
         return;
      }
      //2. create token access, refresh
      String accessToken;
      String refreshToken;
      userRepository.save(user);
      try {
         accessToken = jwtUtil.generateAccessToken(
                 user.getId(),
                 user.getEmail(),
                 user.getNickname(),
                 user.getProfileImageUrl(),
                 user.getRole().getKey());
         refreshToken = jwtUtil.generateRefreshToken(user.getId());
         user.updateRefreshToken(refreshToken);
      } catch (Exception e) {
         handleAuthenticationError(request, response, "auth_token", "ERROR");
         return;
      }
      String targetUrl;
      try {
         // 4. buildRedirectUrl targetUrl
         targetUrl = buildRedirectUrl(user.getRole(), accessToken);
         getRedirectStrategy().sendRedirect(request, response, targetUrl);
         log.info("\nJWT_payload: \n  ID [{}] \n  EMAIL [{}] \n  nick [{}] \n  ROLE [{}]\n{}  ", user.getId(), user.getEmail(), user.getNickname(), user.getRole().getKey(), targetUrl);
      } catch (Exception e) {
         log.error("OAuth2 인증 처리 중 예외 발생: {}", e.getMessage(), e);
         handleAuthenticationError(request, response, "auth_redirect", "인증 처리 중 서버 오류가 발생했습니다.");
      }

   }

   /**
    * 유저 역할에 따라 리다이렉트할 URL 생성
    *
    * @param role User Role
    * @param jwtToken 생성된 JWT 토큰
    * @return 리다이렉트 URL
    */
   private String buildRedirectUrl(Role role, String jwtToken) {
      String targetUri = Role.GUEST.equals(role) ? signupUri : baseUri;
      return UriComponentsBuilder.fromUriString(targetUri)
              .queryParam("token", jwtToken)
              .build()
              .toUriString();
   }

   /**
    * 인증 실패 시 에러 페이지로 리다이렉트
    *
    * @param response HttpServletResponse
    * @param errorCode 에러 코드
    * @param errorMessage 에러 메시지
    */
   private void handleAuthenticationError(HttpServletRequest request, HttpServletResponse response, String errorCode, String errorMessage) throws IOException {
      String errorUrl = UriComponentsBuilder.fromUriString(baseUri)
              .queryParam("error", errorCode)
              .queryParam("message", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
              .build()
              .toUriString();
      getRedirectStrategy().sendRedirect(request, response, errorUrl);
   }
}