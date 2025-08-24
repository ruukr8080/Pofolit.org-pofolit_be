package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.security.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

   private final JwtService jwtService;

   /**
    * 리프레시 토큰을 사용하여 액세스 토큰을 재발급하는 API입니다.
    */
   @PostMapping("/token/refresh")
   public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> requestBody) {
      String refreshToken = requestBody.get("refreshToken");
      if(refreshToken == null) {
         log.error("유효하지 않거나 만료 된 리프레시 토큰입니다.");
         return ResponseEntity.status(401).body(Map.of("message", "Invalid Refresh Token"));
      }
      String newAccessToken = jwtService.refreshAccessToken(refreshToken);
      return ResponseEntity.ok(Map.of("token", newAccessToken));
   }
}
