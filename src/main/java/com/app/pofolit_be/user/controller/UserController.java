package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.security.auth.UserPrincipal;
import com.app.pofolit_be.user.dto.SignupRequest;
import com.app.pofolit_be.user.dto.UserResponseDto;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * &#064;RestController : 사용자 관련 API를 처리합니다.
 * <p>사용자의 프로필 조회, 정보 수정, 계정 삭제 등 개인 계정 관리를 위한 기능을 제공합니다.</p>
 * <p>모든 엔드포인트는 /api/v1/users 경로 아래에 위치합니다.</p>
 *
 * @author Pofolit-BE Team
 * @apiNote 이 컨트롤러는 사용자 인증 이후에 접근 가능한 리소스에 대한 작업을 담당합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * &#064;GET : 현재 로그인된 사용자의 상세 정보를 조회합니다.
     *
     * @param userPrincipal 현재 인증된 사용자 정보
     * @return 사용자 정보 DTO를 포함한 200 OK 응답
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUserDetails(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UUID myId = userPrincipal.getUser().getId();
        UserResponseDto userResponseDto = userService.getUserInfo(myId);
        return ResponseEntity.ok(userResponseDto);
    }

    /**
     * &#064;PATCH : 서명 후 추가 정보를 입력받아 사용자 정보를 업데이트합니다.
     *
     * @param userPrincipal 현재 인증된 사용자 정보를 담은 객체
     * @param signupRequest 추가로 입력할 사용자 정보를 담은 객체
     * @return 성공 시 200 OK,
     * 실패 시 401 Unauthorized
     */
    @PatchMapping("/signup")
    public ResponseEntity<Void> signup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SignupRequest signupRequest) {
        userService.signup(userPrincipal.getUser().getId(), signupRequest);
        return ResponseEntity.ok().build();
    }
}
