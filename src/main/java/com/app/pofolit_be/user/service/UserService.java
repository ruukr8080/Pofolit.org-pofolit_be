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
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * OAuth2 로그인, 회원가입, 사용자 정보 조회 등의 기능을 제공합니다.
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
                    existingUser.updateUser(userDto.nickname(), userDto.profileImageUrl(), userDto.refreshToken());
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
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        log.debug("회원가입 요청: 사용자 ID [{}], 요청 데이터 [{}]", userId, signupRequest);

        user.signup(signupRequest);

        log.info("회원가입 완료: ID [{}], EMAIL [{}], 닉네임 [{}], 프로필이미지 [{}], ROLE [{}], 생년월일 [{}], 직업 [{}], 분야 [{}], 리프레시토큰 [{}]",
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                StringUtils.hasText(user.getProfileImageUrl()) ? "있음" : "없음",
                user.getRole().getKey(),
                user.getBirthDay(),
                user.getJob(),
                user.getDomain(),
                StringUtils.hasText(user.getRefreshToken()) ? "있음" : "없음"
        );
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
