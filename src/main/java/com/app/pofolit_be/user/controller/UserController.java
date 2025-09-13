package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.security.authentication.OIDCUser;
import com.app.pofolit_be.user.dto.SignupRequest;
import com.app.pofolit_be.user.dto.UserResponseDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
     * 현재 인증된 사용자의 세부 정보를 반환합니다.
     * <p>
     * {@code @AuthenticationPrincipal}을 사용하여 {@code SecurityContextHolder}에 저장된
     * {@code OIDCUser} 객체를 직접 가져옵니다.
     * </p>
     *
     * @param oidcUser JwtFilter 인증된 사용자의 Principal
     * @return 사용자의 세부 정보를 담은 {@code UserResponseDto}
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUserDetails(@AuthenticationPrincipal OIDCUser oidcUser) {
        // @AuthenticationPrincipal을 통해 주입된 oidcUser 객체에서 User 엔티티를 가져옵니다.
        User user = oidcUser.getUser();
        log.info("\n/api/v1/users/me API 호출\n[{}]", user.getEmail());
        String providerId = user.getProviderId();
        log.info("OIDC유저-providerId \n[{}]", providerId);
        UserResponseDto userResponseDto = UserResponseDto.from(user);
        log.info("OIDC유저-userResponseDto \n[{}]", userResponseDto);
        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping("/avatar")
    public ResponseEntity<String> getUserAvatar(@AuthenticationPrincipal OIDCUser oidcUser) {
        log.info("Avatar 요청한 OIDC유저 \n[{}]", oidcUser.getEmail());
        User user = oidcUser.getUser();
        String providerId = user.getProviderId();
        UserResponseDto userResponseDto = userService.getUser(providerId);
        String userPictureFromDB = userResponseDto.profileImageUrl();
        if(userPictureFromDB == null)
            return ResponseEntity.ok(oidcUser.getUser().getProfileImageUrl());
        return ResponseEntity.ok(userPictureFromDB);
    }

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
    @PatchMapping("/signup")
    public ResponseEntity<Void> signup(
            @AuthenticationPrincipal OIDCUser oidcUser,
            @RequestBody SignupRequest signupRequest) {
        log.info("/api/v1/users/signup API 호출. 사용자 이메일: {}", oidcUser.getEmail());
//        userService.signup(oidcUser.getUser().getId(), signupRequest);
        return ResponseEntity.ok().build();
    }
}
