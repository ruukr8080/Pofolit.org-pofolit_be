package com.app.pofolit_be.security.auth.jwt;

import com.app.pofolit_be.user.dto.UserPrincipal;
import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT 토큰 기반 인증 필터
 * HTTP 요청에서 JWT 토큰 추출하고 검증해서 Spring Security 컨텍스트에 인증 정보 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

   private final JwtUtil jwtUtil;

   @Override
   protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
      try {
         String token = getJwtFromRequest(request);
         log.info("뽑은 토큰{}\n EXP {}",token,request.getHeader("META"));
         // 2. 토큰 유효성 검증
         if(StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 3. 토큰에서 사용자 정보 추출 (DB 조회 X)
            UserPrincipal userPrincipal = createUserPrincipalFromToken(token);

            // 4. Spring Security 인증 객체 생성 및 Context에 저장
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userPrincipal, null, userPrincipal.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug(
                    "JWT 인증 성공: userId={}, email={}, role={}",
                    userPrincipal.getUser().getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getAuthorities());
         }
      } catch (Exception e) {
         log.error("JWT 필터에서 인증 처리 중 오류 발생: {}", e.getMessage());
         // 인증 실패 시 SecurityContext 클리어
         SecurityContextHolder.clearContext();
      }

      filterChain.doFilter(request, response);
   }

   /**
    * DB 조회 없이 토큰의 Claim만으로 UserPrincipal 객체를 생성한다.
    * @param token 유효성이 검증된 JWT
    * @return UserPrincipal 객체
    */
   private UserPrincipal createUserPrincipalFromToken(String token) {
      UUID userId = jwtUtil.getUserIdFromToken(token);
      String email = jwtUtil.getEmailFromToken(token);
      String roleKey = jwtUtil.getRoleFromToken(token);
      User user = User.builder()
              .id(userId)
              .email(email)
              .role(Role.fromKey(roleKey))
              .build();
      return new UserPrincipal(user, jwtUtil.getAllClaimsFromToken(token));
   }

   /**
    * HTTP 요청에서 JWT 토큰 추출
    * Authorization 헤더에서 Bearer 토큰 형식으로 추출함
    */
   private String getJwtFromRequest(HttpServletRequest request) {
      String bearerToken = request.getHeader("Authorization");

      if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
         return bearerToken.substring(7); // "Bearer " 접두사 제거
      }

      return null;
   }

   /**
    * 특정 경로는 JWT 필터 적용 제외
    * 공개 API나 OAuth2 로그인 경로 등
    */
   @Override
   protected boolean shouldNotFilter(HttpServletRequest request) {
      String path = request.getRequestURI();

      // 공개 경로들은 JWT 필터 건너뜀
      return path.startsWith("/oauth2/") ||
              path.startsWith("/api/v1/auth") ||
              path.startsWith("/login") ||
              path.startsWith("/api/public/") ||
              path.equals("/") ||
              path.startsWith("/swagger-ui/") ||
              path.startsWith("/v3/api-docs");
   }
}

