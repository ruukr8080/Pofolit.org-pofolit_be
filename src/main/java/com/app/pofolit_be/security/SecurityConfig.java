package com.app.pofolit_be.security;

import com.app.pofolit_be.security.auth.OAuth2AuthSuccessHandler;
import com.app.pofolit_be.security.auth.jwt.JwtFilter;
import com.app.pofolit_be.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정 클래스
 * JWT + OAuth2 콤보 인증 시스템 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

   private final JwtFilter jwtFilter;
   private final OAuth2UserService oAuth2UserService;
   private final OAuth2AuthSuccessHandler oAuth2AuthSuccessHandler;

   private static final String[] PERMIT_URLS = {
           "/",
           "/login/**",
           "/oauth2/**",
           "/swagger-ui/**",
           "/v3/api-docs/**",
           "/token",
//           "/api/v1/users/**"
   };


   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
              .cors(cors -> cors.configurationSource(corsConfigurationSource()))
              .csrf(AbstractHttpConfigurer::disable)
//              .formLogin(AbstractHttpConfigurer::disable)
              .httpBasic(AbstractHttpConfigurer::disable)

              .sessionManagement(session -> session
                      .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .authorizeHttpRequests(auth -> auth
                      .requestMatchers(PERMIT_URLS).permitAll()
                      .anyRequest().authenticated()
              )
              .oauth2Login(oauth2 -> oauth2
                      .userInfoEndpoint(userInfo -> userInfo
                              .userService(oAuth2UserService))
                      .successHandler(oAuth2AuthSuccessHandler) // 로그인 성공 시 JWT 토큰 발급
              )
              // UsernamePasswordFilter 앞에서 JWT 필터가 먼저 필터링.
              .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

      return http.build();
   }

   @Bean
   public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration cors = new CorsConfiguration();
      cors.setAllowedOrigins(List.of("http://localhost:3000"));
      cors.setAllowedHeaders(List.of("Authorization", "Content-Type")); // 실제 사용하는 헤더만 명시적으로 허용
      cors.setAllowedMethods(
              Arrays.asList(
                      HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                      HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));
      cors.setAllowCredentials(true);
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", cors);
      return source;
   }
}