package com.app.pofolit_be.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    // 권한(Role) 문자열을 Spring Security Authority 객체로 변환하기 위해 필요
    private static final String ROLE_CLAIM = "role";
    private final JwtDecoder jwtDecoder; // SecurityConfig에서 빈으로 등록된 JwtDecoder 주입
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludePaths = Arrays.asList(
            "/api/v1/auth/**",
            "/login/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    private static UsernamePasswordAuthenticationToken getToken(final String userId, final String role) {
        if(userId == null || role == null) {
            // 필수 클레임이 누락된 경우
            throw new BadJwtException("Missing required claims (sub or role).");
        }

        // 4. 권한 부여 객체 생성 (SimpleGrantedAuthority)
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
        );

        // 5. Authentication 객체 생성 (Stateless 인증)
        // JWT는 상태 비저장이므로 UserDetails 객체 대신 클레임 정보만 담습니다.
        // principal과 authorities를 받는 생성자를 사용해야 isAuthenticated()가 true로 설정됨.
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId, // principal identifier (사용자 식별자)
                        authorities
                );
        return authentication;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 AccessToken 추출
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            log.info("Bearer토큰 필터에서 추출할 때 [{}]", authHeader);
        }

        if(token == null) {
            // 토큰이 없거나 형식이 잘못된 경우, 다음 필터로 진행 (인증되지 않은 상태로)
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. JWT 디코딩 및 유효성 검증
            // JwtDecoder는 서명, 만료 시간(exp) 등을 자동으로 검증합니다 [6-8].
            Jwt jwt = jwtDecoder.decode(token);

            // 3. 클레임 추출 (Subject=User ID, Role=권한)
            String userId = jwt.getSubject(); // User ID (Long을 문자열로 변환한 값) [3-5]
            String role = jwt.getClaimAsString(ROLE_CLAIM); // Role (예: "LV0", "LV1") [3-5]

            UsernamePasswordAuthenticationToken authentication = getToken(userId, role);

            // 6. SecurityContextHolder에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (BadJwtException e) {
            // 토큰이 만료되었거나, 서명이 유효하지 않거나, 기타 JWT 관련 오류 발생 시
            log.debug("Invalid JWT token");
            // SecurityContext를 비우고 (혹시 모를 잔여 상태 방지) 다음 필터로 진행
            SecurityContextHolder.clearContext();

            // API 서버는 401 Unauthorized를 반환해야 하며, 이는 SecurityConfig의 ExceptionHandler에서 처리될 수 있습니다
            // 이 필터에서는 오류를 던지지 않고, 컨텍스트를 비운 채 진행하여 인증 실패로 처리되게 합니다.
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // `permitAll`로 지정된 경로는 SecurityConfig에서 처리하므로,
        // 이 필터는 모든 요청을 일단 검사하도록 설정하는 것이 더 명확함.
        // 토큰이 없는 요청은 doFilterInternal 내부에서 알아서 다음 필터로 넘어감.
        return excludePaths.stream()
                .anyMatch(path -> pathMatcher.match(path, request.getServletPath()));
        //        return false;
    }

}
