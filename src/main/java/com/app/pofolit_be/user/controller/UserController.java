package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.user.dto.CustomUserDetails;
import com.app.pofolit_be.user.dto.UserDetailsDto;
import com.app.pofolit_be.user.repository.UserRepository;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

   private final UserService userService;


   @PatchMapping("/me/details")
   public ResponseEntity<Void> registration(
           @AuthenticationPrincipal CustomUserDetails userDetails,
           @RequestBody UserDetailsDto requestDto) {
      userService.completeRegistration(userDetails.getUser().getId(), requestDto);
      log.info("권한 변경 성공 [{}]",userDetails.getUser().getRole());
      return ResponseEntity.ok().build();
   }
}
