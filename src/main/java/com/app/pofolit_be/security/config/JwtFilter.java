package com.app.pofolit_be.security.config;

import com.app.pofolit_be.security.authentication.OIDCUser;
import com.app.pofolit_be.security.authentication.OIDCUserService;
import com.app.pofolit_be.security.token.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 토큰 기반 '인증 처리 필터'입니다.
 * <p>
 * HTTP 요청 헤더에서 토큰을 추출하고,
 * 토큰의 유효성을 검증한 후,
 * {@code SecurityContextHolder}의 인증 정보를 설정합니다.
 * </p>
 * <p>
 * 이 필터는 Spring Security 필터 체인에서 한 번만 실행되도록
 * {@link OncePerRequestFilter}를 상속받습니다.
 * </p>
 *
 * @author 치킨
 * @apiNote 토큰인증이 필요한 모든 요청을 이 필터에 태우게 하며,
 * 토큰 검증 로직을 중앙 집중화하는 역할을 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final OIDCUserService oidcUserService;

    /**
     * HTTP 요청을 필터링 합니다.
     * <p>1. {@link TokenService}의 토큰추출 메서드{@code createUserPrincipalFromToken()}를 사용합니다.</p>
     * <p>2. 토큰이 유효하면, 토큰에서 사용자 정보를 얻어 {@link OIDCUser} 객체를 생성합니다.</p>
     * <p>3. {@code UsernamePasswordAuthenticationToken}을 생성하여 Spring Security Context에 인증 정보를 박아줍니다.</p>
     * <p>이 과정을 통해({@code AuthorizationFilter})와 같은 나머지 필터들이 사용자의 인증 정보를 참조할 수 있게 됩니다.</p>
     * <p></p>
     *
     * @param request 요청 객체
     * @param response 응답 객체
     * @param filterChain 다음 필터에 필터체이닝해주는 파이프라인
     * @throws ServletException 서블릿 예외가 발생할 경우
     * @throws IOException I/O 예외가 발생할 경우
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authInstance = jwtUtil.extractToken(request);

            if(authInstance != null && jwtUtil.isValid(authInstance)) {
                String providerId = jwtUtil.getSubject(authInstance);
                UserDetails userDetails = oidcUserService.loadUserByUsername(providerId);

                UsernamePasswordAuthenticationToken verified = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                verified.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(verified);
            }
        } catch (Exception e) {
            log.warn("\nJWT 필터에서 인증 처리 중 오류 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }


    /**
     * Public Endpoint.
     * </p>
     * 필터링이 무의미한 요청을 설정했습니다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/") ||
                path.equals("/health") ||
                path.startsWith("/error") ||
                path.startsWith("/login/") ||
                path.startsWith("/signup/") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/api/v1/login/") ||
                path.startsWith("/api/v1/public/");
    }
}
