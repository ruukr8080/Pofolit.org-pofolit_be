package com.app.pofolit_be.security.token;

import com.app.pofolit_be.security.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * HTTP 쿠키 유틸리티 클래스
 * 토큰을 쿠키로 내보내고,
 * 검증 실패시 만료시간을 0으로 만들어 폐기시킵니다.
 */
@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final JwtProperties tokenProperties;
    private final Environment env;

    /**
     * 쿠키를 만료상태로 만들어버립니다.
     * (max-age=0)
     */
    public void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 요청에서 쿠키를 잡아냅니다.
     */
    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(name))
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
     * `OAuth2AuthorizationRequest` 같은 자바 객체를 쿠키에 넣기 위해 직렬화합니다.
     */
    public String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    /**
     * 쿠키에 저장된 문자열을 자바 객체로 역직렬화합니다.
     */
    public <T> T reverseSerialize(Cookie cookie, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookie.getValue())));
    }

    /**
     * 쿠키를 생성하고 공통 속성을 설정합니다.
     * 'accessToken'의 경우 httpOnly=false, maxAge=5분으로 특별 처리합니다.
     * <p>
     * 스프링이 `prod`프로필로 활성화돼있으면 {@code cookie.setSecure(true)}
     * 주요 보안 설정: HttpOnly, Secure, SameSite=Strict
     *
     * @param response HttpServletResponse
     * @param name cookie
     * @param value cookie hash
     */
    public void addCookie(HttpServletResponse response, String name, String value) {
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");
        Cookie cookie = new Cookie(name, value);

        if("pre".equals(name)) {
            cookie.setHttpOnly(false); // 프론트엔드 JS에서 접근 가능해야 함
        } else {
            cookie.setHttpOnly(true); // 그 외 쿠키는 JS 접근 불가
        }

        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }
}