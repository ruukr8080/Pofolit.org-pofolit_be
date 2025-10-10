package com.app.pofolit_be.user.service;

import com.app.pofolit_be.user.dto.UserDto.UserCreateRequest;
import com.app.pofolit_be.user.dto.UserDto.UserResponse;
import com.app.pofolit_be.user.dto.UserDto.UserUpdateRequest;
import com.app.pofolit_be.user.dto.UserMapper;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;


  @Transactional
  public UserResponse upsertUser(UserCreateRequest request) {

    // 1. 기존 사용자 조회: provider + subject 조합으로 유니크하게 조회
    Optional<User> existingUser = userRepository.findBySubject(request.subject());

    User user;
    if (existingUser.isPresent()) {
      // 2. 이미 존재하는 사용자: Oauth 정보로 업데이트 (현재 로직에서는 프로필 정보만 업데이트)
      user = existingUser.get();
      // NOTE: User Entity에 @Setter(role)만 있고 다른 필드의 setter가 없으므로,
      // UserRequest의 모든 필드를 Entity에 덮어쓰는 매퍼를 사용해야 합니다.
      user = userMapper.toEntity(request); // 새로운 DTO로 Entity를 다시 생성 (Full Update)

    } else {
      // 3. 신규 사용자: DTO를 Entity로 변환 후 저장
      user = userMapper.toEntity(request);
      user = userRepository.save(user);
    }

    return userMapper.toResponse(user);
  }

  public UserResponse getUserProfile(Long subject) {
    User user = userRepository.findById(subject)
        .orElseThrow(() -> new EntityNotFoundException("사용자 subject를 찾을 수 없습니다: " + subject));
    return userMapper.toResponse(user);
  }

  @Transactional
  public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자 ID를 찾을 수 없습니다: " + userId));

    // Mapper를 사용하여 요청 DTO의 Non-null 필드만 Entity에 반영
    userMapper.partialUpdate(request, user);

    // 저장 로직은 @Transactional에 의해 자동 처리되지만, 명시적 호출도 가능합니다.
    user = userRepository.save(user);

    return userMapper.toResponse(user);
  }
}
