package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.security.service.AuthService;
import com.app.pofolit_be.security.token.TokenValidator;
import com.app.pofolit_be.user.dto.SignupRequest;
import com.app.pofolit_be.user.dto.UserResponseDto;
import com.app.pofolit_be.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * &#064;PATCH : 사용자 정보를 받아 검증+사용자 정보를 업뎃합니다.
 *
 * @param signupRequest 사용자 추가정보
 * @return 200
 * <p>
 * 검증 :
 * `@AuthenticationPrincipal(UserPrincipal.instance)`
 * 저장 :
 * 1. UserController.signup(UserRequest.instance)
 * 3. User.signup(UserRequest.instance).
 * -> Role.User
 */

/**
 * &#064;RestController : 사용자 관련 API를 처리합니다.
 * <p>모든 엔드포인트는 /api/v1/users 경로를 포합합니다.</p>
 * @apiNote 인증 이후에 리소스 접근 권한별 처리합니다
 *
 * @author 치킨
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    private final AuthService authService;
    private final UserService userService;
    private final TokenValidator validator;

    @GetMapping("/avatar")
    public ResponseEntity<UserResponseDto> getUserAvatar(
            @CookieValue(value = "pre", required = false) String preToken) {
        if(preToken != null && !preToken.isEmpty()) {
            try {
                Claims claims = validator.parseClaims(preToken);
                UserResponseDto guestDto = UserResponseDto.fromPreTokenClaims(claims);
                return ResponseEntity.ok(guestDto);
            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PatchMapping("/signup")
    public ResponseEntity<Void> signup(
            @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok().build();
    }

}
