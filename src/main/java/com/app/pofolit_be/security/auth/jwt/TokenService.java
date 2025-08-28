package com.app.pofolit_be.security.auth.jwt;

import com.app.pofolit_be.common.exceptions.CustomException;
import com.app.pofolit_be.security.auth.UserPrincipal;
import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

   private final TokenGenerator tokenGenerator;
   private final UserRepository userRepository;

   public UserPrincipal createUserPrincipalFromToken(String token) {
      Claims claims = tokenGenerator.getAllClaimsFromToken(token);
      User user = User.builder()
              .id(UUID.fromString(claims.getSubject()))
              .email(claims.get("email", String.class))
              .role(Role.fromKey(claims.get("role", String.class)))
              .build();
      return new UserPrincipal(user, claims);
   }

   @Transactional
   public Map<String, String> issueToken(User user) {
      String accessToken = tokenGenerator.generateAccessToken(user);
      String refreshToken = tokenGenerator.generateAccessToken(user);
      user.updateRefreshToken(refreshToken);

      Map<String, String> tokens = new HashMap<>();
      tokens.put("accessToken", accessToken);
      tokens.put("refreshToken", refreshToken);

      return tokens;
   }

   @Transactional
   public Map<String, String> reIssueToken(String token) {

      if(!tokenGenerator.validateToken(token)) {
         throw new IllegalStateException();
      }
      var userId = tokenGenerator.getUserIdFromToken(token);
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. : " + userId));
      if(!token.equals(user.getRefreshToken())) {
         throw new CustomException("re login plz", HttpStatus.BAD_REQUEST, "TOKEN_MISMATCH");
      }
      String newAccessToken = tokenGenerator.generateAccessToken(user);
      String newRefreshToken = tokenGenerator.generateRefreshToken(user);
      user.updateRefreshToken(newRefreshToken);

      Map<String, String> newTokens = new HashMap<>();
      newTokens.put("accessToken", newAccessToken);
      newTokens.put("refreshToken", newRefreshToken);
      return newTokens;
   }

   public String getTokenFromRequest(HttpServletRequest request) {
      String bearerToken = request.getHeader("Authorization");
      if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
         log.debug("Token found in Authorization header");
         return bearerToken.substring(7);
      }
      Cookie[] cookies = request.getCookies();
      if(cookies != null) {
         for (Cookie cookie : cookies) {
            if("accessToken".equals(cookie.getName())) {
               log.debug("Token found in 'accessToken' cookie");
               return cookie.getValue();
            } else if("refreshToken".equals(cookie.getName())) {
               log.debug("Token found in 'refreshToken' cookie");
               return cookie.getValue();
            }
         }
      }
      return null;
   }

   public void ResponseTokenByCookie(HttpServletResponse res, String token) {
      ResponseCookie cookie = ResponseCookie.from("accessToken", token)
              .httpOnly(true)
              .secure(true)
              .path("/")
              .maxAge(300)
              .sameSite("Lax")
              .build();
      res.addHeader("Set-Cookie", cookie.toString());
   }

}
