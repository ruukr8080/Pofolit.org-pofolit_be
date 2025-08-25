package com.app.pofolit_be.user.service;

import com.app.pofolit_be.common.exceptions.ApiResponse;
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
                 return existingUser;
              })
              .orElseGet(() -> {
                 User newUser = User.builder()
                         .email(userDto.email())
                         .nickname(userDto.nickname())
                         .profileImageUrl(userDto.profileImageUrl())
                         .registrationId(userDto.registrationId())
                         .providerId(userDto.providerId())
                         .role(Role.GUEST)
                         .build();
                 return userRepository.save(newUser);
              });
   }

   @Transactional(readOnly = true)
   public User getUserByEmail(String email) {
      return userRepository.findUserByEmail(email).orElseThrow(() ->
              new IllegalArgumentException("User not found with email " + email));
   }

   @Transactional
   public void signup(UUID userId, SignupRequest signupRequest) {
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("nono" + userId));

      if(!user.getRole().equals(Role.GUEST)) {
         throw new IllegalStateException("already member");
      }
      user.signup(signupRequest);
      log.info("\nJOIN : \n  ID [{}] \n  EMAIL [{}] \n  nick [{}] \n  프사[{}]\n  ROLE [{}]\n \n  Birth [{}] \n  직업 [{}] \n  분야 [{}]  관심사 목록 [{}]\n 토큰 : '{}'",
              user.getId(), user.getEmail(), user.getNickname(), StringUtils.hasText(user.getProfileImageUrl()) ? "no image" : "O",
              user.getBirthDay(), user.getJob(), user.getDomain(), user.getInterests(),
              user.getRole().getKey(), !StringUtils.hasText(user.getRefreshToken()) ? "없음" : "있음");
   }

   @Transactional
   public UserResponseDto getUserInfo(UUID userid) {
      User user = userRepository.findById(userid)
              .orElseThrow(()-> new IllegalArgumentException("NO userid"));
      log.info("response! [{}] [{}]", user.getEmail(), user.getNickname());
      return UserResponseDto.from(user);
   }
}
//nickname: decodedPayload.nickname,
//profileImageUrl: decodedPayload.profileImageUrl,
//birthDay: decodedPayload.birthDay,
//domain: decodedPayload.domain,
//job: decodedPayload.job,
//interests: decodedPayload.interests,