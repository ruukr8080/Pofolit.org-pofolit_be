package com.app.pofolit_be.security.auth.jwt;

import com.app.pofolit_be.common.exception.CustomException;
import com.app.pofolit_be.security.auth.UserPrincipal;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 토큰 관련 비즈니스 로직을 처리하는 서비스입니다.
 * <p>
 * 사용자 인증에 필요한 JWT 토큰의 생성, 검증, 재발급을 담당합니다.
 * </p>
 *
 * @author 치킨
 * @apiNote 인증/인가 과정에서
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

    private final TokenGenerator tokenGenerator;
    private final UserRepository userRepository;

    /**
     * JwtToken의 Claims에서 userId 추출 하고 {@link UserPrincipal}객체를 빌드.
     * <p>
     * Jwtfilter를 태워서 토큰을 검증하고, Spring-Security의
     * {@code SecurityContextHolder}에 userId가 담긴 인증객체(UserPrincipal)를 설정합니다.
     * </p>
     *
     * @param token String
     * @return 토큰 정보로 생성된 {@code UserPrincipal} 객체
     */
    public UserPrincipal createUserPrincipalFromToken(String token) {
        Claims claims = tokenGenerator.getAllClaimsFromToken(token);
        User user = User.builder()
                .id(UUID.fromString(claims.getSubject()))
                .build();
        return new UserPrincipal(user, claims);
    }

    /**
     * 특정 사용자에 대한 새로운 accessToken과 refreshToken을 발급합니다.
     * <p>
     * token은 DB에 저장되어 reIssueToken()에서 이용됩니다.
     * </p>
     *
     * @param user User
     * @return tokens{
     * "accessToken" : token
     * "refreshToken" : token
     * }
     */
    @Transactional
    public Map<String, String> issueToken(User user) {
        log.debug("토큰 발급 요청: 사용자 ID [{}], EMAIL [{}]", user.getId(), user.getEmail());

        String token = tokenGenerator.generateRefreshToken(user);
        user.updateRefreshToken(token);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", token);
        tokens.put("refreshToken", token);

        log.info("토큰 발급 완료: 사용자 ID [{}]", user.getId());
        return tokens;
    }

    /**
     * refreshToken 검증하고 accessToken,refreshToken을 재발급합니다.
     * <p>
     * 토큰 재발급 요청(String token) -> 토큰 검증  -> userId 조회+검증, userId를 pk로 가진 refreshToken값과 비교검증.
     * 모든 검증이 성공하면 새로운 accessToken,refreshToken을 사용자에게 발급해주고 DB의 refreshToken을 갱신합니다.
     * </p>
     *
     * @param token 검증할 refreshToken
     * @return 재발급된 tokens{
     * "accessToken" : token
     * "refreshToken" : token
     * }
     * @throws IllegalStateException 유효하지 않은 토큰일 경우
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
     * @throws CustomException 리프레시 토큰이 일치하지 않는 경우
     */
    @Transactional
    public Map<String, String> reIssueToken(String token) {
        log.debug("토큰 재발급 요청: 토큰 검증 시작");

        if(!tokenGenerator.validateToken(token)) {
            throw new IllegalStateException("유효하지 않은 토큰입니다");
        }

        UUID userId = tokenGenerator.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        if(!token.equals(user.getRefreshToken())) {
            throw new CustomException("리프레시 토큰이 일치하지 않습니다. 다시 로그인해 주세요", HttpStatus.BAD_REQUEST, "TOKEN_MISMATCH");
        }

        String newAccessToken = tokenGenerator.generateAccessToken(user);
        String newRefreshToken = tokenGenerator.generateRefreshToken(user);
        user.updateRefreshToken(newRefreshToken);

        Map<String, String> newTokens = new HashMap<>();
        newTokens.put("accessToken", newAccessToken);
        newTokens.put("refreshToken", newRefreshToken);

        log.info("토큰 재발급 완료: 사용자 ID [{}]", userId);
        return newTokens;
    }

    /**
     * 쿠키에서 토큰을 추출합니다.
     * <p>
     * 요청 헤더의 {@code Authorization} 필드(Bearer 토큰)를 먼저 확인하고
     * 없을 경우 쿠키에서 "accessToken" 또는 "refreshToken"을 찾습니다.
     * 만일 EXP가 상대적으로 더 긴 refreshToken가 없는데 accessToken이 있는 경우는 XSS 공격을 의심하여
     * null을 반환하여 로그인페이지로 리다이렉트하게 유도합니다.
     * </p>
     *
     * @param request 토큰을 가져 올 요청 객체
     * @return 추출된 refreshToken,refreshToken이 없으면 {@code null}
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.debug("요청에 포함 된 쿠키 토큰 [{}]", bearerToken);
            return bearerToken.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if("refreshToken".equals(cookie.getName())) {
                    log.debug("cookie name : 'refreshToken'");
                    return cookie.getValue();
                } else if("accessToken".equals(cookie.getName())) {
                    log.debug("cookie name : 'accessToken'");
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * accessToken을 HTTP 응답 쿠키에 추가해줍니다.
     * <p>
     * 로그인 성공 시 클라이언트에게 토큰을 전달합니다.
     * </p>
     * <p>
     * 보안 설정 :
     * 1. {@code httpOnly}
     * 2. {@code secure}
     * 3. {@code SameSite=Lax}
     * </p>
     *
     * @param res 토큰 담아 줄 응답 객체
     * @param token 쿠키에 담을 액세스 토큰
     */
    public void responseTokenByCookie(HttpServletResponse res, String token) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(300)
                .sameSite("Lax")
                .build();
        res.addHeader("Set-Cookie", cookie.toString());
    }

}
