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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

   private final UserService userService;

   @GetMapping("/me")
   public ResponseEntity<?> getUserDetails(
           @AuthenticationPrincipal UserPrincipal userPrincipal) {

      UUID myId = userPrincipal.getUser().getId();
      UserResponseDto userResponseDto = userService.getUserInfo(myId);
      return ResponseEntity.ok(userResponseDto);
   }

   /**
    * update or register after login
    */
   @PatchMapping("/signup")
   public ResponseEntity<Void> signup(
           @AuthenticationPrincipal UserPrincipal userPrincipal,
           @RequestBody SignupRequest signupRequest) {
      userService.signup(userPrincipal.getUser().getId(), signupRequest);
      return ResponseEntity.ok().build();
   }
}
