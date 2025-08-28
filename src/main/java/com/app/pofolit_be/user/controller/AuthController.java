package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.security.auth.CookieUtil;
import com.app.pofolit_be.security.auth.jwt.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

   private final TokenService tokenService;

   @PostMapping("/token/refresh")
   public ResponseEntity<Void> refreshTokens(
           @CookieValue(value = "refreshToken", required = false) String refreshToken,
           HttpServletResponse response) {
      if(refreshToken == null) {
         return ResponseEntity.status(401).build();
      }
      try {
         Map<String, String> newTokens = tokenService.reIssueToken(refreshToken);
         String newAccessToken = newTokens.get("accessToken");
         String newRefreshToken = newTokens.get("refreshToken");
         CookieUtil.addTokensToCookie(response, newAccessToken, newRefreshToken);
         tokenService.ResponseTokenByCookie(response, newTokens.get("accessToken"));

         return ResponseEntity.ok().build();
      } catch (IllegalArgumentException e) {
         CookieUtil.expireCookie(response, "accessToken");
         CookieUtil.expireCookie(response, "refreshToken");

         return ResponseEntity.status(401).build();
      }
   }
}