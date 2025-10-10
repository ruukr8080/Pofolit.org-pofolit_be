package com.app.pofolit_be.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

  private final JwtDecoder jwtDecoder;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();
  private final List<String> skipPaths = Arrays.asList(
      "/api/v1/auth/refresh",
      "/login/**",
      "/swagger-ui/**",
      "/v3/api-docs/**"
  );

  private static UsernamePasswordAuthenticationToken getToken(String key, String role) {

    if (key == null || role == null) {
      throw new BadJwtException("파라미터 누락");
    }
    List<GrantedAuthority> authorities = Collections.singletonList(
        new SimpleGrantedAuthority(role.toUpperCase())
    );
    return new UsernamePasswordAuthenticationToken(
        key, // principal
        null, // credentials은 업음
        authorities // authorities
    );
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String requestPath = request.getRequestURI();
    boolean shouldSkip = skipPaths.stream().anyMatch(
        path -> pathMatcher.match(path, requestPath)
    );
    if (shouldSkip) {
      log.debug("JWT검증 스킵: {}", requestPath);
      filterChain.doFilter(request, response);
      return;
    }
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    String token = null;
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      token = authHeader.substring(7);
    }
    if (token == null) {
      log.warn("Bearer token [{}]", authHeader);
      filterChain.doFilter(request, response);
      return;
    }
    try {
      Jwt jwt = jwtDecoder.decode(token);
      String userSub = jwt.getSubject();
      String role = jwt.getClaimAsString("role");
      UsernamePasswordAuthenticationToken authentication = getToken(userSub, role);
      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (BadJwtException e) {
      log.warn("[401] JWT token Unauthorized");
      SecurityContextHolder.clearContext();
    }
    filterChain.doFilter(request, response);
  }
}
