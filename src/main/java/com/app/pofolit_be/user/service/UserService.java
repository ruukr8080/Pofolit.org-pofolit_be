package com.app.pofolit_be.user.service;

import com.app.pofolit_be.common.exception.CustomException;
import com.app.pofolit_be.common.exception.ExCode;
import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.dto.UserResponseDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 비즈니스 로직입니다.
 * <p>
 * 1. updateOrSaveUser() :
 * - 신규사용자: GUEST 권한으로 회원가입.
 * - 기존사용자: 소셜 정보 업데이트.
 * 2. signup() :
 * </p>
 * TODO:User user; 떄문에 생기는 레이스 컨디션 (Race Condition)
 * 만약 동일한 신규 유저가 거의 동시에 두 번 로그인 요청을 보낸다면?
 * `@Transactional`로 블록 전체가 하나의 원자적인 DB 작업으로 묶이도록 하는 거야.
 * 이렇게 하면 DB의 유니크 제약 조건에 의해 동시성 문제가 발생했을 때 자동으로 예외를 던져주기 때문에 더 안전.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserResponseDto getUser(String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new UsernameNotFoundException("providerId로 조회 실패.: " + providerId));
        return UserResponseDto.from(user);
    }

}
