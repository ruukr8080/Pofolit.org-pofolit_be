package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.user.dto.SignupRequest;
import com.app.pofolit_be.user.dto.UserPrincipal;
import com.app.pofolit_be.user.dto.UserResponseDto;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

   private final UserService userService;

   /*
    * current user's detail info  */
   @GetMapping("/me")
   public ResponseEntity<UserResponseDto> getUserDetails(
           @AuthenticationPrincipal UserPrincipal userPrincipal) {
      UserResponseDto userResponseDto = userService.getUserInfo(userPrincipal.getUser().getId());
      return ResponseEntity.ok(userResponseDto);
   }

   /*
    * update or register after login  */
   @PatchMapping("/signup")
   public ResponseEntity<Void> signup(
           @AuthenticationPrincipal UserPrincipal userPrincipal,
           @RequestBody SignupRequest signupRequest) {
      userService.signup(userPrincipal.getUser().getId(), signupRequest);
      return ResponseEntity.ok().build();
   }
}
