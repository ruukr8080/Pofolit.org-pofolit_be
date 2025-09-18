package com.app.pofolit_be.user.service;

import com.app.pofolit_be.common.exception.CustomException;
import com.app.pofolit_be.common.exception.ExCode;
import com.app.pofolit_be.security.authentication.AuthenticatedUser;
import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.dto.SignupRequest;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.app.pofolit_be.security.SecurityLevel.LV0;
import static com.app.pofolit_be.security.SecurityLevel.LV1;

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

    public void saveOrUpdateUser(SignDto signDto) {
        userRepository.findBySocialOrEmail(
                        signDto.registrationId(),
                        signDto.providerId(),
                        signDto.email()
                )
                .map(user -> {// 같은 이메일인데 소셜 계정 정보가 다를때,
                    if(!user.getRegistrationId().equals(signDto.registrationId()) ||
                            !user.getProviderId().equals(signDto.providerId())) {
                        throw new CustomException(ExCode.DUPLICATE_ACCOUNT);
                    }
                    return updateUser(user, signDto);
                })
                .orElseGet(() -> saveUser(signDto));
    }

    public User updateUser(User user, SignDto signDto) {
        user.updateUser(signDto);
        if(user.getSecurityLevel() == null) {
            user.setSecurityLevel(LV0);
        }
        else if (user.getSecurityLevel() == LV0) {
            user.setSecurityLevel(LV1);
        }
        return user;
    }

    private User saveUser(SignDto signDto) {
        return userRepository.save(signDto.toEntity());
    }



    // TODO: 맘에 안드는 메서드. 개선하고싶다.: 필터 통과하는 Authenticate객체를 만들때 최대한 DB접근(userEntity) 안하기, 책임분리 다 위반.
    //  1. DB접근을 하되 엔티티에 providerId만 빼서 oidcUser구현체한테 전달하기 실패.
    //  - 그럼 적어도 DB조회할땐 providerId 말고 user_id로 조회해서 조회시간이라도 줄여보자.
    //  어차피 가입할때 providerId 생성되니까 user_id를providerId로 사용하기. -> 큰일날듯.
    public AuthenticatedUser getAuthenticatedUserFrom(String providerIdFromUserEntity) {
        User user = getUserProviderId(providerIdFromUserEntity);

        // 이 시점에는 OidcUser 객체가 없으므로 null을 전달합니다.
        return AuthenticatedUser.fromUserEntity(user);
    }
    public User getUserProviderId(String providerId) {
        return userRepository.findByProviderId(providerId);
    }
    public User getUserById(long id) {
        return userRepository.findUserById(id);
    }

    private User userSignup(User user, SignupRequest signupRequest) {
        user.signup(signupRequest);
        if(user.getSecurityLevel() == LV0) {
            user.setSecurityLevel(LV1);
        }
        return user;
    }

}
