package com.app.pofolit_be.security;

import com.app.pofolit_be.common.exception.ApiResponse;
import com.app.pofolit_be.security.auth.AuthSuccessHandler;
import com.app.pofolit_be.security.auth.jwt.TokenFilter;
import com.app.pofolit_be.security.auth.properties.TokenProperties;
import com.app.pofolit_be.user.service.SignService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정 클래스
 * JWT + OAuth2 콤보 인증 시스템 설정
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(TokenProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PERMIT_URLS = {
            "/api/auth/**",
            "/favicon.ico",
            "/health",
            "/login/**",
            "/swagger-ui/**",
            "/v3/**"
    };
    private final TokenFilter tokenFilter;
    private final SignService signService;
    private final AuthSuccessHandler authSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_URLS).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(authSuccessHandler)
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(signService)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:3000"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setAllowedMethods(
                Arrays.asList(
                        HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));
        cors.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authEntryPoint() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return (req, res, exc) -> {
            log.warn("\n[{}]\n[{}]", req.getRequestURI(), exc.getMessage());
            ApiResponse errorResponse =
                    ApiResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .error("UNAUTHORIZED")
                            .message("인증이 필요합니다. 로그인을 먼저 진행해주세요.")
                            .path(req.getRequestURI())
                            .build();
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };

    }
}
