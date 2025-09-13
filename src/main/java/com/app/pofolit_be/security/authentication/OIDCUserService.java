package com.app.pofolit_be.security.authentication;

import com.app.pofolit_be.security.authentication.OIDCUser;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관례상 네이밍은 `CustomUserDetailsService`입니다.
 * JWT 토큰 인증 과정에서 사용자 정보를 로드하는 합니다.
 * 시쿠리티 사용자 정보를 찾는 서비스.
 * 인증에 필요한 사용자 정보를 조회하는 기능.
 *
 * <p>
 * - JWT 기반 인증 {@link UserDetails}
 * - OIDC 기반 인증 {@link OidcUser}
 * </p>
 */
@Service("userDetailsService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OIDCUserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security의 loadUserByUsername 인터페이스를 구현.
     * JWT 토큰의 subject(providerId)로 사용자를 죄회해서 OIDCUser 인스턴스를 반환.
     *
     * @param providerId "subject" (providerId)
     * @return OIDCUser.instance (UserDetails 인터페이스 구현체)
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String providerId) throws UsernameNotFoundException {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new UsernameNotFoundException("providerId로 조회 실패.: " + providerId));
        return OIDCUser.from(user);
    }
}
