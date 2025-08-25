package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.user.dto.SignupRequest;
import com.app.pofolit_be.user.dto.UserPrincipal;
import com.app.pofolit_be.user.dto.UserResponseDto;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

   private final UserService userService;

   /**
    * current user's detail info
    * @params nickname,profileImageUrl,birthDay,domain,job,interests,
    */
   @GetMapping("/me")
   public ResponseEntity<UserResponseDto> getUserDetails(
           @AuthenticationPrincipal UserPrincipal userPrincipal) {
      if(!StringUtils.hasText(userPrincipal.getUsername())) {
       return ResponseEntity.status(401).build();
      }
      // 1. 지연 설정. 2. 트랜잭션 설정.
      UserResponseDto userResponseDto = userService.getUserInfo(userPrincipal.getUser().getId());

      return  ResponseEntity.ok(userResponseDto);
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
// access(alg.sub,exp,) : eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2YTAyOGFhYS00OTg2LTRiYzQtODY0NS1mM2JmNWU4NjYwMmIiLCJpYXQiOjE3NTYxMjU5MjAsImV4cCI6MTc1NjEzMjUyMH0.NvfVnQbftBH16uobC5x6gDA5PveIzojqv_0Ltmtbyr8bI_TqnIqHDGil92LizefiEhEyIqTr_4rCAp_HeoDoJg
// ref : eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiUk9MRV9VU0VSIiwibmlja25hbWUiOiJHb28iLCJwcm9maWxlSW1hZ2VVcmwiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NJRWFZa3pvTW1UeVpoSk5zdkVqelpxbXI1czFtQ0NlZUlsWlZjcHBCSC1XNDhFb3 lpLT1zOTYtYyIsImVtYWlsIjoiZm91cmZpcnN0MUBnbWFpbC5jb20iLCJzdWIiOiI2YTAyOGFhYS00OTg2LTRiYzQtODY0NS1mM2JmNWU4NjYwMmIiLCJpYXQiOjE3NTYxMjU5MjAsImV4cCI6MTc1NjEyOTUyMH0.rq9Xur5opJRzHtwusqsoEKzLo1Va99-uSAsjdGMaPxBm1iDnBBykpq6gIB8aOAhcxa3iwXZlVBXqS1rD28ZFMw