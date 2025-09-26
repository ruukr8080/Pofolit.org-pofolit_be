package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.security.dto.TokenPair;
import com.app.pofolit_be.user.dto.UserDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * @param accessToken JWT
     * @return 200 OK와 함께 유저 정보 반환
     * @apiNote 현재 로그인한 유저의 정보를 조회합니다.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('LV1') or hasRole('LV0')")
    public ResponseEntity<UserDto> getMyInfo(@AuthenticationPrincipal String accessToken) {
        //        NimbusJwtDecoder.
        //        decord()accessToken.
        //        AuthenticationPrincipal.class.getDeclaredMethod("getToken");
        //        User user = userService.getUserById();

        //        UserDto responseDto = new UserDto(
        //                user.getEmail(),
        //                user.getNickname(),
        //                user.getAvatar(),
        //                user.getProvider(),
        //                user.getSubject(),
        //                user.getAccess()
        //        );
        //        return ResponseEntity.ok(responseDto);
        return ResponseEntity.ok(null);
    }

    /**
     * @param userId JWT의 Subject 클레임으로 인증된 유저 객체 (사용자 ID)
     * @param userDto 닉네임, 프로필 이미지 등 추가 정보
     * @return 200 OK
     * @apiNote 최초 로그인(LV0) 유저의 회원가입을 완료합니다.
     */
    @PatchMapping("/signup")
    @PreAuthorize("hasRole('LV0')")
    public ResponseEntity<TokenPair> signup(
            @AuthenticationPrincipal String userId,
            @RequestBody UserDto userDto) {
        try {
            User user = userService.getUserById(Long.parseLong(userId));
            userService.completeSignup(user, userDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
