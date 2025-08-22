package com.app.pofolit_be.user.service;

import com.app.pofolit_be.user.dto.OAuth2UserDto;
import com.app.pofolit_be.user.dto.SignupRequest;
import com.app.pofolit_be.user.dto.UserResponseDto;
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

import java.util.Optional;
import java.util.UUID;

/*
 * Guest로 저장 된 유저 signup or
 * USER로 저장 된 유저 update. upadte는 user에.
 *
 * */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
   private final UserRepository userRepository;

   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public User updateOrSaveUser(OAuth2UserDto userDto) {
      if(userRepository.findUserByEmail(userDto.email()).isEmpty()){
         User newUser = User.builder()
                 .email(userDto.email())
                 .nickname(userDto.nickname())
                 .profileImageUrl(userDto.profileImageUrl())
                 .role(Role.GUEST)
                 .build();
         return userRepository.save(newUser);
      }else {
          User existingUser = User.builder()
                  .nickname(userDto.nickname())
                  .profileImageUrl(userDto.profileImageUrl())
                  .build();
         return userRepository.save(existingUser);
      }
   }
   @Transactional(readOnly = true)
   public User getUserByEmail(String email) {
      return userRepository.findUserByEmail(email).orElseThrow(() ->
              new IllegalArgumentException("User not found with email " + email));
   }

   @Transactional
   public void signup(UUID userId, SignupRequest signupRequest) {
      User user = userRepository.findById(userId)
              .orElseThrow(()-> new IllegalArgumentException("nono"+userId));

      if(!user.getRole().equals(Role.GUEST)){
         throw new IllegalStateException("already member");
      }
      user.signup(signupRequest);
      log.info("가입 완료! [{}]", user.getEmail());
   }

   @Transactional
   public UserResponseDto getUserInfo(UUID userid) {
      User user = userRepository.findById(userid)
              .map(u ->{
                 u.getEmail();
                 u.getNickname();
                 u.getProfileImageUrl();
                 log.info("res! [{}] [{}] [{}]",u.getEmail(),u.getNickname(),u.getProfileImageUrl());
                 return u;
              })
              .orElseThrow(() -> new IllegalArgumentException("userid 없음"));
      log.info("response! [{}] [{}]",user.getEmail(),user.getNickname());
      return UserResponseDto.from(user);
   }
}
