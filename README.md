### 개발 환경 (Development Environment)
> backend port : http://localhost:8080/  
> frontend port : http://localhost:3000/
- **Java 17**, **Spring Boot 3.5.4** , **Gradle-jar**
- IDE: **IntelliJ IDEA**
- Management process: **Notion** _sprint_style_
- Database: { **MySQL**,**H2(인메모리)**,**redis** }
- CI/CD: { **Jenkins**, **Docker** } 
- Dependencies:
  - JPA , mapstruct , flyway-core 
  - Spring-Security ,  JWT 0.12.6 , OAuth2: _kakao,google_
  - JUnit , Mockito
  - Swagger-ui
- 패키지 구조
    ```txt
   └─pofolit_be
       │  PofolitBeApplication.java
       │
       ├─common
       │  ├─exceptions
       │  │      ErrorResponse.java
       │  │      GlobalExceptionHandler.java
       │  │
       │  └─utils
       │          ResponseUtil.java
       │
       ├─security
       │  └─auth
       │      │  OAuth2AuthSuccessHandler.java
       │      │  SecurityConfig.java
       │      │
       │      └─jwt
       │              JwtFilter.java
       │              JwtUtil.java
       │
       └─user
           ├─dto
           │  │  CustomUserDetails.java
           │  │  OAuth2UserDto.java
           │  │
           │  └─converter
           │          Google.java
           │          Kakao.java
           │          OAuth2Converter.java
           │
           ├─entity
           │      Role.java
           │      User.java
           │
           ├─repository
           │      UserRepository.java
           │
           └─service
                  OAuth2UserService.java
   ```
