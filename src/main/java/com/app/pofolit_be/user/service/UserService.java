package com.app.pofolit_be.user.service;

import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.dto.SignupRequest;
import com.app.pofolit_be.user.dto.UserResponseDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 비즈니스 로직을 처리합니다.
 * `oauth2 API`로부터 발급받은 토큰을 1차로 저장합니다.
 * <p>
 * OAuth2 로그인 - 신규사용자: GUEST 권한으로 회원가입.
 * - 기존사용자: 소셜 정보 업데이트.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User updateOrSaveUser(SignDto signDto) {
        Optional<User> optionalUser = userRepository.findByRegistrationIdAndProviderId(signDto.registrationId(), signDto.providerId());
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.update(signDto);
            return user;
        }else {
            Optional<User> userByEmailOptional = userRepository.findUserByEmail(signDto.email());
            if (userByEmailOptional.isPresent()) {
                User existingUser = userByEmailOptional.get();
                throw new IllegalArgumentException("이미 " + existingUser.getRegistrationId().toUpperCase()
                        + " 로 가입된 계정이 있습니다."
                );
            }
            return userRepository.save(signDto.toEntity());
        }
    }

    @Transactional
    public void signup(UUID userId, SignupRequest signupRequest) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        log.debug("회원가입 요청: 사용자 ID [{}], 요청 데이터 [{}]", userId, signupRequest);
        user.signup(signupRequest);
    }

    @Transactional
    public UserResponseDto getUserInfo(UUID userid) {
        log.debug("사용자 정보 조회 요청: 사용자 ID [{}]", userid);
        User user = userRepository.findUserById(userid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userid));
        UserResponseDto response = UserResponseDto.from(user);
        log.info("사용자 정보 조회 완료: EMAIL [{}], 닉네임 [{}]", user.getEmail(), user.getNickname());
        return response;
    }
}
