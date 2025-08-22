package com.app.pofolit_be.user.controller;

import com.app.pofolit_be.security.auth.jwt.JwtUtil;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

   private final JwtUtil jwtUtil;
   private final UserRepository userRepository;

   /**
    * 리프레시 토큰을 사용하여 액세스 토큰을 재발급하는 API입니다.
    */
   @PostMapping("/token/refresh")
   public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody Map<String, String> requestBody) {
      String refreshToken = requestBody.get("refreshToken");

      if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
         log.error("유효하지 않은 리프레시 토큰입니다.");
         return ResponseEntity.status(401).body(Map.of("message", "Invalid Refresh Token"));
      }

      try {
         UUID userId = jwtUtil.getUserIdFromToken(refreshToken);
         User user = userRepository.findById(userId)
                 .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

         if (!refreshToken.equals(user.getRefreshToken())) {
            log.error("DB에 저장된 토큰과 일치하지 않습니다. userId: {}", userId);
            return ResponseEntity.status(401).body(Map.of("message", "Refresh Token Mismatch"));
         }

         // 새로운 액세스 토큰 발급
         String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(),user.getNickname(),user.getProfileImageUrl(), user.getRole().getKey());

         // 리프레시 토큰도 재발급
         String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
         user.updateRefreshToken(newRefreshToken);
         userRepository.save(user);

         Map<String, String> response = new HashMap<>();
         response.put("accessToken", newAccessToken);
         response.put("refreshToken", newRefreshToken);

         return ResponseEntity.ok(response);

      } catch (io.jsonwebtoken.ExpiredJwtException e) {
         log.error("만료된 리프레시 토큰: {}", e.getMessage());
         return ResponseEntity.status(401).body(Map.of("message", "Expired Refresh Token"));
      } catch (Exception e) {
         log.error("토큰 재발급 중 오류 발생: {}", e.getMessage());
         return ResponseEntity.status(500).body(Map.of("message", "Internal Server Error"));
      }
   }
}
