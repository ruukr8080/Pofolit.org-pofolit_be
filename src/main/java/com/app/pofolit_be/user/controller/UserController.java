package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.user.dto.UserDto.UserResponse;
import com.app.pofolit_be.user.dto.UserDto.UserUpdateRequest;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  @PreAuthorize("hasAnyAuthority('LV0', 'LV1')")
  public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
    Long subject = Long.parseLong(authentication.getName());
    UserResponse userProfile = userService.getUserProfile(subject);
    return ResponseEntity.ok(userProfile);
  }

  @PatchMapping("/me")
  @PreAuthorize("hasAnyAuthority('LV0', 'LV1')")
  public ResponseEntity<UserResponse> updateProfile(Authentication authentication,
      @RequestBody UserUpdateRequest userUpdateDto) {
    Long subject = Long.parseLong(authentication.getName());
    UserResponse updateUser = userService.updateProfile(subject, userUpdateDto);
    return ResponseEntity.ok(updateUser);
  }
}
