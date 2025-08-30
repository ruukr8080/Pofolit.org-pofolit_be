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

/**
 * 토큰 기반 '인증 처리 필터'입니다.
 * <p>
 *     HTTP 요청 헤더에서 토큰을 추출하고,
 *     토큰의 유효성을 검증한 후,
 *     {@code SecurityContextHolder}의 인증 정보를 설정합니다.
 * </p>
 * <p>
 *     이 필터는 Spring Security 필터 체인에서 한 번만 실행되도록
 *     {@link OncePerRequestFilter}를 상속받습니다.
 * </p>
 *
 * @apiNote 토큰인증이 필요한 모든 요청을 이 필터에 태우게 하며,
 *          토큰 검증 로직을 중앙 집중화하는 역할을 합니다.
 * @author 치킨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final TokenGenerator jwtUtil;
    private final TokenService tokenService;
    /**
     * HTTP 요청을 필터링 합니다.
     * <p>1. {@link TokenService}의 토큰추출 메서드{@code createUserPrincipalFromToken()}를 사용합니다.</p>
     * <p>2. 토큰이 유효하면, 토큰에서 사용자 정보를 얻어 {@link UserPrincipal} 객체를 생성합니다.</p>
     * <p>3. {@code UsernamePasswordAuthenticationToken}을 생성하여 Spring Security Context에 인증 정보를 박아줍니다.</p>
     * <p>이 과정을 통해({@code AuthorizationFilter})와 같은 나머지 필터들이 사용자의 인증 정보를 참조할 수 있게 됩니다.</p>
     *
     * @param request  요청 객체
     * @param response  응답 객체
     * @param filterChain 다음 필터에 필터체이닝해주는 파이프라인
     * @throws ServletException 서블릿 예외가 발생할 경우
     * @throws IOException I/O 예외가 발생할 경우
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = tokenService.getTokenFromRequest(request);
            if(StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                UserPrincipal userPrincipal = tokenService.createUserPrincipalFromToken(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal, null, userPrincipal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.debug("JWT 필터 요청 값  : {}", e.getMessage());
            log.error("JWT 필터에서 인증 처리 중 오류 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
    /**
     * TokenFilter가 거르지 않는 HTTP 요청들입니다.
     * <p>
     *     인증이 필요없다 판단되는 퍼블릭 엔드포인트(예: loginApi, joinApi, swaggerUI 등...)에서
     *     필터가 무의미하게 실행되지 않습니다.
     *     {@code path.startsWith("/자유패스/")}에 해당하는 요청경로는 다음 필터로 패스합니다.
     * </p>
     *
     * @param request 퍼블릭 요청
     * @return 필터 실행 성공 : {@code false}, 필터 실행 실패 {@code true}
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/oauth2/") ||
                path.startsWith("/api/v1/auth") ||
                path.startsWith("/login") ||
                path.startsWith("/api/public/") ||
                path.equals("/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }
}
