package com.app.pofolit_be.security.auth.jwt;

import com.app.pofolit_be.security.auth.UserPrincipal;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

   private final TokenGenerator jwtUtil;
   private final TokenService tokenService;

   @Override
   protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
      try {
         String token = tokenService.getTokenFromRequest(request);
         log.info("Request on Jwt filter | Token found: {}", StringUtils.hasText(token));
         if(StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            UserPrincipal userPrincipal = tokenService.createUserPrincipalFromToken(token);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userPrincipal, null, userPrincipal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
         }
      } catch (Exception e) {
         log.error("JWT 필터에서 인증 처리 중 오류 발생: {}", e.getMessage());
         SecurityContextHolder.clearContext();
      }
      filterChain.doFilter(request, response);
   }
   @Override
   protected boolean shouldNotFilter(HttpServletRequest request) {
      String path = request.getRequestURI();
      return path.startsWith("/oauth2/") ||
              path.startsWith("/api/v1/auth") || // 로그인, 토큰 재발급 등
              path.startsWith("/login") ||
              path.startsWith("/api/public/") ||
              path.equals("/") ||
              path.startsWith("/swagger-ui/") ||
              path.startsWith("/v3/api-docs");
   }
}
