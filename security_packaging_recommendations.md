# Security 패키지 구조 개선 제안

## 현재 구조 분석

### 문제점
1. **일관성 부족**: 일부 클래스는 하위 패키지에, 일부는 루트에 위치
2. **기능별 그룹핑 미흡**: 관련 기능들이 흩어져 있어 유지보수성 저하
3. **의존성 복잡도**: 루트 레벨 클래스들이 하위 패키지를 참조하는 역방향 의존성

### 현재 구조
```
security/
├── SecurityConfig.java           # 메인 설정
├── OIDCUser.java                # 사용자 모델
├── OIDCUserService.java         # 사용자 서비스
├── AuthSuccessHandler.java      # OAuth2 성공 핸들러
├── AuthService.java             # 빈 서비스 (미구현)
├── token/                       # 토큰 관련
│   ├── JwtUtil.java
│   ├── JwtFilter.java
│   ├── CookieUtil.java
│   ├── CookieRepository.java
│   └── RedisUtil.java
└── properties/                  # 설정 클래스
    ├── JwtProperties.java
    ├── TokenRequest.java
    └── TokenResponse.java
```

## 개선안 1: 기능별 분류 (권장)

### 구조
```
security/
├── config/                      # 설정 관련
│   ├── SecurityConfig.java      
│   ├── JwtProperties.java       # properties에서 이동
│   └── CorsConfig.java          # 분리 가능 시
├── authentication/              # 인증 관련
│   ├── OIDCUser.java            # 루트에서 이동
│   ├── OIDCUserService.java     # 루트에서 이동
│   └── AuthSuccessHandler.java  # 루트에서 이동
├── token/                       # 토큰 관리 (유지)
│   ├── JwtUtil.java
│   ├── JwtFilter.java
│   ├── CookieUtil.java
│   ├── CookieRepository.java
│   └── RedisUtil.java
├── dto/                         # DTO 클래스
│   ├── TokenRequest.java        # properties에서 이동
│   └── TokenResponse.java       # properties에서 이동
└── service/                     # 서비스 클래스
    └── AuthService.java         # 루트에서 이동 (향후 구현 예정)
```

### 장점
1. **명확한 책임 분리**: 각 패키지가 고유한 역할을 담당
2. **직관적인 구조**: 개발자가 쉽게 찾고 이해할 수 있음
3. **확장성**: 새로운 기능 추가 시 적절한 위치를 쉽게 결정
4. **유지보수성**: 관련 파일들이 함께 위치하여 수정 용이

## 개선안 2: 계층별 분류 (대안)

### 구조
```
security/
├── config/                      # 설정 계층
│   ├── SecurityConfig.java
│   └── JwtProperties.java
├── web/                         # 웹 계층 (필터, 핸들러)
│   ├── JwtFilter.java           # token에서 이동
│   ├── AuthSuccessHandler.java  # 루트에서 이동
│   ├── CookieUtil.java          # token에서 이동
│   └── CookieRepository.java    # token에서 이동
├── service/                     # 서비스 계층
│   ├── OIDCUserService.java     # 루트에서 이동
│   ├── AuthService.java         # 루트에서 이동
│   ├── JwtUtil.java             # token에서 이동
│   └── RedisUtil.java           # token에서 이동
├── model/                       # 모델 계층
│   └── OIDCUser.java            # 루트에서 이동
└── dto/                         # DTO 계층
    ├── TokenRequest.java
    └── TokenResponse.java
```

## 구현 가이드

### 1단계: 새 패키지 생성
```bash
mkdir -p src/main/java/com/app/pofolit_be/security/config
mkdir -p src/main/java/com/app/pofolit_be/security/authentication
mkdir -p src/main/java/com/app/pofolit_be/security/dto
mkdir -p src/main/java/com/app/pofolit_be/security/service
```

### 2단계: 파일 이동 및 패키지 선언 수정
1. **config 패키지로 이동**
   - `JwtProperties.java` (properties → config)
   - 패키지 선언: `package com.app.pofolit_be.security.config;`

2. **authentication 패키지로 이동**
   - `OIDCUser.java` (root → authentication)
   - `OIDCUserService.java` (root → authentication)
   - `AuthSuccessHandler.java` (root → authentication)

3. **dto 패키지로 이동**
   - `TokenRequest.java` (properties → dto)
   - `TokenResponse.java` (properties → dto)

4. **service 패키지로 이동**
   - `AuthService.java` (root → service)

### 3단계: Import 구문 수정
모든 참조 클래스들의 import 구문을 새로운 패키지 경로로 업데이트

### 예상 Import 수정 사항
```java
// SecurityConfig.java
import com.app.pofolit_be.security.config.JwtProperties;
import com.app.pofolit_be.security.authentication.AuthSuccessHandler;

// AuthController.java
import com.app.pofolit_be.security.service.AuthService;
import com.app.pofolit_be.security.dto.TokenRequest;
import com.app.pofolit_be.security.dto.TokenResponse;

// JwtFilter.java
import com.app.pofolit_be.security.authentication.OIDCUser;
import com.app.pofolit_be.security.authentication.OIDCUserService;
```

## 권장 사항

### 개선안 1을 권장하는 이유
1. **도메인 중심**: Spring Security의 기능별 특성에 맞게 구성
2. **실무 표준**: 대부분의 Spring Boot 프로젝트에서 사용하는 패턴
3. **확장성**: OAuth2, JWT 외 다른 인증 방식 추가 시에도 유연함
4. **가독성**: 패키지명만으로도 기능을 쉽게 파악 가능

### 구현 우선순위
1. **1단계**: config, dto 패키지 정리 (가장 간단)
2. **2단계**: authentication 패키지 정리 (의존성 주의)
3. **3단계**: service 패키지 정리 및 빈 구현체 정리

### 추가 개선 사항
1. **AuthService 구현 완성**: 현재 빈 클래스를 실제 기능으로 구현
2. **SecurityConfig 분리**: CORS 설정 등을 별도 클래스로 분리 검토
3. **테스트 패키지도 동일한 구조로 정리**: 일관성 유지

이 구조 개선을 통해 코드의 가독성, 유지보수성, 확장성을 크게 향상시킬 수 있습니다.