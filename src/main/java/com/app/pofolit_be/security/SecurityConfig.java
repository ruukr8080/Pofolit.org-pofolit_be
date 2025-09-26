package com.app.pofolit_be.security;

import com.app.pofolit_be.security.config.AuthenticationFilter;
import com.app.pofolit_be.security.service.SignService;
import com.app.pofolit_be.security.util.CustomAccessDeniedHandler;
import com.app.pofolit_be.security.util.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Lazy
    private final SignService signService;
    @Lazy
    private final AuthenticationFilter jwtFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(source -> source.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                //                 3. 예외 처리 핸들러 (JWT 검증 실패 시 401/403 응답 등)
                .oauth2Login(oauth2 -> oauth2
                                .userInfoEndpoint(userInfo -> userInfo
                                        .oidcUserService(signService)
                                )
                                .successHandler(signService)
                        // OIDC 인증 실패 시 처리 로직도 추가되어야 함
                )
                // 5. JWT 기반 Access Token 검증 필터 등록
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)   // 인증되지 않은 요청에 대한 진입점 (401 Unauthorized 처리)
                        .accessDeniedHandler(accessDeniedHandler) // 인가 실패 (403 Forbidden) 핸들러
                )
                .authorizeHttpRequests(authorize -> authorize
                        // (OAuth 2.1에서 인가 코드는 PKCE로 보호됨 [6-8])
                        .requestMatchers(
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**",
                                "/health", "/signup/**",
                                "/login/**", // OIDC 로그인 시작/리다이렉션 경로
                                "/auth/login/**",
                                "/api/v1/auth/refresh" // 토큰 갱신 API (Refresh Token은 쿠키로 전달되므로, 이 경로를 필터에서 제외하거나 특별 처리해야 함)
                        ).permitAll()
                        // 보호된 엔드포인트: 유효한 Access Token이 필요함
                        .anyRequest().authenticated()
                )
                // 6. Refresh Token Rotation을 위한 HttpOnly 쿠키 사용 시 필요 (미구현)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::disable));

        return http.build();
    }

}