package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.user.dto.ProfileDto;
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
    @PreAuthorize("hasAuthority('LV0') or hasAuthority('LV1')")
    public ResponseEntity<ProfileDto> getProfile(Authentication authentication) {
        String principal = authentication.getName();
        log.info("Authentication: {}", authentication);

        User user = userService.getUserBySubject(principal);
        if(user == null) {
            log.warn("User not found for subject: {}", principal);
            return ResponseEntity.notFound().build();
        }

        log.info("User found: {}", user.getEmail());
        return ResponseEntity.ok(ProfileDto.from(user));
    }

    @PatchMapping("/signup")
    @PreAuthorize("hasAuthority('LV0') or hasAuthority('LV1')")
    public ResponseEntity<Void> signup(Authentication authentication, @RequestBody UserDto userDto) {
        try {

            String subject = authentication.getName();
            User user = userService.getUserBySubject(subject);

            userService.completeSignup(user, userDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
