package com.app.pofolit_be.security.auth.jwt;

import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtService {
   private final JwtUtil jwtUtil;
   private final UserRepository userRepository;


   /**
    * 로그인 성공 후 액세스 토큰과 리프레시 토큰을 발급하고, 리프레시 토큰은 DB에 저장한다.
    * @param user 인증된 유저 엔티티
    * @return 생성된 액세스 토큰
    */
   @Transactional
   public String issueTokensAndSave(User user) {
      // 1. 액세스 토큰 생성
      String accessToken = jwtUtil.generateAccessToken(user);
//      String accessToken = jwtUtil.generateAccessToken(
//              user.getId(),
//              user.getEmail(),
//              user.getNickname(),
//              user.getProfileImageUrl(),
//              user.getRole().getKey());
      // 2. 리프레시 토큰 생성 및 DB 저장
      String refreshToken = jwtUtil.generateRefreshToken(user.getId());
      user.updateRefreshToken(refreshToken);
      userRepository.save(user); // 변경 감지(dirty checking)로도 동작하지만, 명시적으로 save 호출
      return accessToken;
   }

   /**
    * 리프레시토큰으로 새로운 액세스 토큰 재발급.
    *
    * @param refreshToken
    * @return new accessToken
    */
   @Transactional
   public String refreshAccessToken(String refreshToken) {
      if(!jwtUtil.validateToken(refreshToken)) {
         throw new IllegalArgumentException("Invalid Refresh Token");
      }

      var userId = jwtUtil.getUserIdFromToken(refreshToken);
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. : " + userId));

      if(!refreshToken.equals(user.getRefreshToken())) {
         // 옛날 토큰으로 재발급 시도를 의심하는 부분. 탈취됐던 토큰일 가능성이 높다.
         // 해당 유저의 ref토큰을 DB에서 삭제하고 강제 로그아웃 시키는게 국룰
         throw new IllegalArgumentException("Refresh Token Mismatch");
      }
//      String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl(), user.getRole().getKey());
      String newAccessToken = jwtUtil.generateAccessToken(user);
      String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
      user.updateRefreshToken(newRefreshToken);
      userRepository.save(user);

      return newAccessToken;
   }
}