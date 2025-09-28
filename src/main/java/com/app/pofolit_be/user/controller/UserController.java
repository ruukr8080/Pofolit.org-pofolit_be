package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.user.dto.UserDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('LV1') or hasRole('LV0')")
    public ResponseEntity<UserDto> getMyInfo(Authentication authentication) {
        // AuthenticationFilter에서 Principal에 userId(String)를 넣어줬음
        String userId = authentication.getName(); //sub
        User user = userService.getUserById(Long.parseLong(userId));

        return ResponseEntity.ok(UserDto.toUser(user));
    }

    @PatchMapping("/signup")
    @PreAuthorize("hasRole('LV0')")
    public ResponseEntity<Void> signup(Authentication authentication, @RequestBody UserDto userDto) {
        try {
            String userId = authentication.getName();
            User user = userService.getUserById(Long.parseLong(userId));

            userService.completeSignup(user, userDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
