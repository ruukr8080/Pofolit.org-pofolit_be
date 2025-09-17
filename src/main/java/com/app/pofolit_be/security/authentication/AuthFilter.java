package com.app.pofolit_be.security.authentication;

import com.app.pofolit_be.common.external.UriPath;
import com.app.pofolit_be.security.service.AuthService;
import com.app.pofolit_be.security.token.ForensicUtil;
import com.app.pofolit_be.security.token.TokenValidator;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private static final Set<String> TOKEN_COOKIE_NAMES = Set.of("pre", "accessToken", "refreshToken");
    private final UriPath excludePath;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final TokenValidator tokenValidator;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractAnyToken(request);
            SecurityContext context = SecurityContextHolder.getContext();
            if(StringUtils.hasText(token)) {
                tokenValidator.validateToken(token);
                Authentication authentication = authService.createAuthentication(token);
                context.setAuthentication(authentication);
                log.info("인증객체 :\n[{}]", context.getAuthentication());
            }
        } catch (ExpiredJwtException ex) {
            log.info("기간만료 : {}", ex.getMessage());
        } catch (SignatureException ex) {
            String clientIp = ForensicUtil.getIp(request);
            String clientOs = ForensicUtil.getOs(request);
            String clientBrowser = ForensicUtil.getBrowser(request);
            String userAgent = request.getHeader("User-Agent");
            log.warn("서명 위조 감지. IP: {}, OS: {}, 브라우저: {}, User-Agent: {}, 오류: {}",
                    clientIp, clientOs, clientBrowser, userAgent, ex.getMessage());
        } catch (MalformedJwtException |
                 UnsupportedJwtException |
                 IllegalArgumentException e) {
            log.error("지원하지 않는 형식입니다.: {}", e.getMessage());
        } catch (Exception e) {
            log.error("인증 실패: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    public String extractAnyToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            Optional<Cookie> tokenCookie = Arrays.stream(cookies)
                    .filter(cookie -> AuthFilter.TOKEN_COOKIE_NAMES.contains(cookie.getName()))
                    .findFirst();
            if(tokenCookie.isPresent()) {
                return tokenCookie.get().getValue();
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return Arrays.stream(excludePath.getFilter()).anyMatch(pattern -> pathMatcher.match(pattern, request.getRequestURI()));
    }
}