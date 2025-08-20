package com.app.pofolit_be.security.auth;

import com.app.pofolit_be.security.auth.jwt.JwtUtil;
import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import com.app.pofolit_be.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
   private final UserRepository userRepository;
   @Value("${uri.auth.base}")
   private String baseUri;
   @Value("${uri.auth.signup}")
   private String signup;

   /**
    * JWT 만들어서 클라로
    */

   @Override
   public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
      try {
         //[페이로드 추출] OIDC로 인증된 사용자 권한 추출
         OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
         log.info("OIDCUserDetails (principal) : {}", oAuth2User.getAttributes());
         //[페이로드 추출] { registrationId , providerId(sub) } from OAuth2LoginAuthenticationToken
         OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
         String registrationId = oauthToken.getAuthorizedClientRegistrationId(); //from Authentication's
         String providerId = oAuth2User.getName(); // from OAuth2User.Principal 안에 name 요소로.
         String nickname = registrationId.equals("google")
                 ? (String) oAuth2User.getAttributes().get("name")
                 : (String) oAuth2User.getAttributes().get("nickname");
         User user = userRepository.findByRegistrationIdAndProviderId(registrationId, providerId)
//                 .orElseThrow(() -> new IllegalStateException("OAuth2 O || findUser X"));
                 .orElseGet(() -> {
                    User newUser = User.builder()
                            .registrationId(registrationId)
                            .providerId(providerId)
                            .email(oAuth2User.getAttribute("email"))
                            .nickname(nickname)
                            .role(Role.GUEST)
                            .build();
                    return userRepository.save(newUser);
                 });

         // JWT 토큰 생성
         String jwtToken = jwtUtil.generateToken(
                 user.getId(),
                 user.getEmail(),
                 user.getRole().getKey());
         log.info("OAuth2 로그인 성공 : [{}] [{}] [{}] [{}]",
                 user.getRole().getKey(), user.getRegistrationId(), user.getEmail(), user.getId());
         // to Front URL with jwt-token
         String targetUrl;
         if(user.getRole() == Role.GUEST) {
            targetUrl = UriComponentsBuilder.fromUriString(signup)
                    .queryParam("token", jwtToken)
                    .build().toUriString();
         } else {
            targetUrl = UriComponentsBuilder.fromUriString(baseUri)
                    .queryParam("token", jwtToken)
                    .build().toUriString();
         }
         log.info("OAuth2 성공 리다이렉트 URL: {}", targetUrl);

         // 리다이렉트 실행
         getRedirectStrategy().sendRedirect(request, response, targetUrl);

      } catch (Exception e) {
         log.error("OAuth2 인증 성공 처리 중 오류 발생: {}", e.getMessage(), e);

         // 오류 발생 시 에러 페이지로 리다이렉트
         String errorUrl = UriComponentsBuilder.fromUriString(baseUri)
                 .queryParam("error", "authentication_failed")
                 .queryParam("message", URLEncoder.encode("인증 처리 중 오류가 발생했습니다", StandardCharsets.UTF_8))
                 .build()
                 .toUriString();
         getRedirectStrategy().sendRedirect(request, response, errorUrl);
      }
   }
}