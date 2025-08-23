package com.app.pofolit_be.security.auth.jwt;

import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtService {
   private final JwtUtil jwtUtil;
   private final UserRepository userRepository;

   /**
    * 로그인 성공 후 액세스 토큰과 리프레시 토큰을 발급하고, 리프레시 토큰은 DB에 저장한다.
    *
    * @param user 인증된 유저 엔티티
    * @return 생성된 액세스 토큰
    */
   @Transactional
   public String issueTokensAndSave(User user) {
      // 1. 액세스 토큰 생성
      String accessToken = jwtUtil.generateAccessToken(
              user.getId(),
              user.getEmail(),
              user.getNickname(),
              user.getProfileImageUrl(),
              user.getRole().getKey());

      // 2. 리프레시 토큰 생성 및 DB 저장
      String refreshToken = jwtUtil.generateRefreshToken(user.getId());
      user.updateRefreshToken(refreshToken);
      userRepository.save(user); // 변경 감지(dirty checking)로도 동작하지만, 명시적으로 save 호출

      return accessToken;
   }
}