### 개발 환경 (Development Environment)
- **Java 17**, **Spring Boot 3.4.7** , **Gradle-jar**
- IDE: **IntelliJ IDEA**
- Management process: **Notion** _sprint_style_
- Database: { **MySQL** }
- CI/CD: { **GitHub Actions**, **Docker** } 
- Dependencies:
  - JPA , mapstruct , flyway-core 
  - Spring-Security ,  JWT 11.5 , OAuth2: _kakao,google_
  - JUnit , Mockito
  - Swagger-ui

> backend port : http://localhost:8080/    
> frontend port : http://localhost:3000/
 
---
### 디렉토리 구조
```text
backend
  ├── application         `application layer` 
  │   ├── dto             // DTO (Data Transfer Objects)
  │   ├── mapper          // MapStruct 매퍼 인터페이스
  │   └── usecase         // 비즈니스 로직 구현체
  │
  ├── domain              `domain layer` 주요 
  │   ├── entity          // JPA 엔티티 클래스
  │   ├── repository      // JPA 리포지토리 인터페이스
  │   └── service           // 도메인 서비스 인터페이스
  │
  └── infrastructure      `infra layer`
      ├── config          // Spring 설정
      ├── controller      // API 엔드포인트 컨트롤러
      ├── exceptions      // 예외 + 핸들러
      ├── security        // JWT, OAuth2 등 보안 관련 코드
      └── utils           // 공통 유틸리티 클래스
```

