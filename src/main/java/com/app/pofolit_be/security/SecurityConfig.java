package com.app.pofolit_be.security;

import com.app.pofolit_be.common.exceptions.ApiResponse;
import com.app.pofolit_be.security.auth.AuthSuccessHandler;
import com.app.pofolit_be.security.auth.SignService;
import com.app.pofolit_be.security.auth.jwt.JwtFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class SecurityConfig {

   private static final String[] PERMIT_URLS = {
           "/favicon.ico",
           "/swagger-ui/**",
           "/v3/**",
           "/login/**",
           "/api/auth/**",

           //           "/api/v1/users/**"
           //            만료된 토큰 처리 -> JwtFilter가 인증 실패 처리(인증 객체 생성 안 함) ->
           //            SecurityConfig가 permitAll()이라 요청 통과 ->
           //            Controller에서 @AuthenticationPrincipal이 null ->
           //            NullPointerException.
   };

   private final JwtFilter jwtFilter;
   private final SignService oAuth2UserService;
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
                      .requestMatchers("/api/v1/users/me").hasAnyRole("GUEST","USER")
                      .anyRequest().authenticated())
              .sessionManagement(session -> session
                      .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .addFilterBefore(jwtFilter,
                      UsernamePasswordAuthenticationFilter.class)
              .oauth2Login(oauth2 -> oauth2
                      .successHandler(authSuccessHandler)
                      .userInfoEndpoint(userInfo -> userInfo
                              .oidcUserService(oAuth2UserService)));
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
      ObjectMapper ojm = new ObjectMapper().registerModule(new JavaTimeModule());
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
         res.getWriter().write(ojm.writeValueAsString(errorResponse));
      };

   }
}
