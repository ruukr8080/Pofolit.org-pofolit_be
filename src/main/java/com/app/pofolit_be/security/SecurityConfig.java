package com.app.pofolit_be.security;

import com.app.pofolit_be.security.auth.OAuth2SuccessHandler;
import com.app.pofolit_be.security.auth.jwt.JwtFilter;
import com.app.pofolit_be.user.service.OAuth2UserService;
import com.app.pofolit_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
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

   private static final String[] PERMIT_URLS = {
           "/favicon.ico",
           "/swagger-ui/**",
           "/v3/**",
           "/login/**",
           "/api/auth/**",
           "/api/v1/users/**"
   };

   private final JwtFilter jwtFilter;
   private final OAuth2UserService oAuth2UserService;
   private final OAuth2SuccessHandler oAuth2SuccessHandler;


   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
              .csrf(AbstractHttpConfigurer::disable)
              .formLogin(AbstractHttpConfigurer::disable)
              .httpBasic(AbstractHttpConfigurer::disable)
              .cors(cors -> cors.configurationSource(corsConfigurationSource()))
              .authorizeHttpRequests(auth -> auth
                      .requestMatchers(PERMIT_URLS).permitAll()
                      .anyRequest().authenticated())
              .sessionManagement(session -> session
                      .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .addFilterBefore(jwtFilter,
                      UsernamePasswordAuthenticationFilter.class)
              .oauth2Login(oauth2 -> oauth2
                      .successHandler(oAuth2SuccessHandler)
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
}
