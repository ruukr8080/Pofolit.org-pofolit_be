package com.app.pofolit_be.security.authentication;

import com.app.pofolit_be.security.service.AuthService;
import com.app.pofolit_be.security.token.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 레디스를 바라보게. 레디스가 죽으면 캐시데이터니까 다죽으니까
 * 레디스를 쓸때는 엔진 인증만 저장하는
 * 레디스 만료 관리를 잘 하고 데이터 메모리를 잘 봐야한다.
 * &#064;RestController  : 토큰 관련 API를 처리합니다.
 * <p>사용자의 토큰을 갱신하는 인증 플로우를 담당합니다.</p>
 * <p>모든 엔드포인트는 /api/v1/auth 경로 아래에 위치합니다.</p>
 *
 * @author 치킨
 * @apiNote 클라이언트의 (Cookie):accessToken 만료 시 (Cookie):refreshToken을 참조하여 토큰 재발급합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    /**
     * 로그아웃 처리
     * <p>
     * 클라이언트의 (Cookie):"accessToken","refreshToken"을 만료시키고,
     * Redis에서 리프레시 토큰을 삭제합니다.
     * </p>
     *
     * @param response http.instance
     * @param refreshToken 클라이언트의 리프레시 토큰 (쿠키에서 추출)
     * @return 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response,
                                       @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        log.info("로그아웃 요청 수신");
        
        authService.logout(refreshToken, response);
        
        log.info("로그아웃 처리 완료");
        return ResponseEntity.ok().build();
    }
}