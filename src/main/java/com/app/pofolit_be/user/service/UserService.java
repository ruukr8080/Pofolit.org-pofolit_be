package com.app.pofolit_be.user.service;

import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.dto.SignupRequest;
import com.app.pofolit_be.user.dto.UserResponseDto;
import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
   private final UserRepository userRepository;

   @Transactional
   public User updateOrSaveUser(SignDto userDto) {
      return userRepository.findByRegistrationIdAndProviderId(userDto.registrationId(), userDto.providerId())
              .map(existingUser -> {
                 existingUser.updateSocialProfile(userDto.nickname(), userDto.profileImageUrl());
                 existingUser.updateRefreshToken(userDto.refreshToken());
                 return existingUser;
              })
              .orElseGet(() -> {
                 User newUser = User.builder()
                         .email(userDto.email())
                         .nickname(userDto.nickname())
                         .profileImageUrl(userDto.profileImageUrl())
                         .registrationId(userDto.registrationId())
                         .refreshToken(userDto.refreshToken())
                         .providerId(userDto.providerId())
                         .role(Role.GUEST)
                         .build();
                 return userRepository.save(newUser);
              });
   }

   @Transactional(readOnly = true)
   public User getUserByEmail(String email) {
      return userRepository.findUserByEmail(email).orElseThrow(() ->
              new IllegalArgumentException("이메일로 사용자를 찾을 수 없습니다: " + email));
   }

   @Transactional
   public void signup(UUID userId, SignupRequest signupRequest) {
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("ID로 사용자를 찾을 수 없습니다: " + userId));
      if(!user.getRole().equals(Role.GUEST)) {
         user.updateRefreshToken(user.getRefreshToken());
         log.info("이미 가입된 회원입니다.");
      }
      user.signup(signupRequest);
      log.info("\nJOIN : \n  ID [{}] \n  EMAIL [{}] \n  NICK [{}] \n  IMAGE [{}]\n  ROLE [{}]\n  BIRTH [{}] \n  JOB [{}] \n  DOMAIN [{}] \n  TOKEN [{}]",
              user.getId(),
              user.getEmail(),
              user.getNickname(),
              StringUtils.hasText(user.getProfileImageUrl()) ? "O" : "X",
              user.getRole().getKey(),
              user.getBirthDay(),
              user.getJob(),
              user.getDomain(),
              StringUtils.hasText(user.getRefreshToken()) ? "O" : "X");
   }

   @Transactional(readOnly = true)
   public UserResponseDto getUserInfo(UUID userid) {
      User user = userRepository.findById(userid)
              .orElseThrow(() -> new IllegalArgumentException("ID로 사용자를 찾을 수 없습니다: " + userid));
      log.info("response! [{}] [{}]", user.getEmail(), user.getNickname());
      return UserResponseDto.from(user);
   }
}
