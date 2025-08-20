package com.app.pofolit_be.security.auth.jwt;

import com.app.pofolit_be.user.dto.CustomUserDetails;
import com.app.pofolit_be.user.dto.OAuth2UserDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import com.app.pofolit_be.user.service.OAuth2UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
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
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. JWT 토큰 추출
            String jwt = getJwtFromRequest(request);
            log.info("jwt 추출 성공[{}]",jwt);
            // 2. 토큰 유효성 검증
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                UUID userId = jwtUtil.getUserIdFromToken(jwt);
                String email = jwtUtil.getEmailFromToken(jwt);
                String role = jwtUtil.getRoleFromToken(jwt);
                log.info("[{}][{}][{}]",userId,email,role);
                // 사용자 정보 조회 (필요시 DB 조회)
                Optional<User> userOptional = userRepository.findById(userId);
                log.info("userOptional [{}]",userOptional);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    
                    // Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            new CustomUserDetails(user),
                            null, 
                            Collections.singleton(new SimpleGrantedAuthority(role))
                        );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Security Context에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("JWT 인증 성공: userId={}, email={}, role={}", userId, email, role);
                } else {
                    log.warn("JWT는 유효하지만 사용자 정보를 찾을 수 없음: userId={}", userId);
                }
            }
        } catch (Exception e) {
            log.error("JWT 필터에서 인증 처리 중 오류 발생: {}", e.getMessage());
            // 인증 실패 시 SecurityContext 클리어
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * HTTP 요청에서 JWT 토큰 추출
     * Authorization 헤더에서 Bearer 토큰 형식으로 추출함
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
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
               path.startsWith("/login") ||
               path.startsWith("/api/public/") ||
               path.equals("/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs");
    }
}
