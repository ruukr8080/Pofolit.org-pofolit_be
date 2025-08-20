package com.app.pofolit_be.user.service;

import com.app.pofolit_be.user.dto.OAuth2UserDto;
import com.app.pofolit_be.user.dto.UserDetailsDto;
import com.app.pofolit_be.user.entity.Role;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
 * Facade service
 *
 * */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
   private final UserRepository userRepository;

   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public User findOrSaveUser(OAuth2UserDto userDto) {
      return userRepository.findByRegistrationIdAndProviderId(userDto.registrationId(), userDto.providerId())
              .map(existingUser -> {
                 existingUser.updateProfile(userDto.nickname(), userDto.profileImageUrl());
                 log.info("정상 로그인 userId{}", existingUser.getId());
                 return existingUser.updateProfile(userDto.nickname(), userDto.profileImageUrl());
              })
              .orElseGet(() -> {
                 userRepository.findUserByEmail(userDto.email()).ifPresent(existingUserByEmail -> {
                    log.warn("가입 전적 있음.email{}", userDto.email());
                    throw new OAuth2AuthorizationException(
                            new OAuth2Error("ErrorCode:email_in_use", "이미 가입 된 이메일입니다", "uri[]"));
                 });
                 log.info("GUEST로 저장.{}", userDto.email());
                 return userRepository.save(userDto.toEntity(Role.GUEST));
              });
   }

   @Transactional
   public void completeRegistration(UUID userid, UserDetailsDto userDetailDto) {
      User user = userRepository.findById(userid)
              .orElseThrow(() -> new IllegalArgumentException("userid 없음"));
      user.completeRegistration(userDetailDto);
   }
}
