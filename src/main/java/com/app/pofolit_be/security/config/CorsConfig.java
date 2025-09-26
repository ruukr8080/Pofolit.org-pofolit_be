package com.app.pofolit_be.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:3000"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cors.setAllowCredentials(true);  // 인증 정보(쿠키, HTTP 인증)
        cors.setMaxAge(3600L);// pre-flight 요청의 결과를 캐싱할 시간(초).

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // 모든 경로에 위에서 설정한 CORS 구성을 적용.
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}

