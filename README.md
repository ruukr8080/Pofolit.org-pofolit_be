### 개발 환경 (Development Environment)

> backend port : http://localhost:8080/  
> frontend port : http://localhost:3000/

- **Java 17**, **Spring Boot 3.5.4**, **Gradle-jar
  **
- IDE: **IntelliJ IDEA**
- Management process: **Notion** _sprint_style_
- Database: **MySQL**, **H2** , **redis(RESP3)**
- CI/CD: **Jenkins** , **Docker**
- Dependencies:
    - JPA , flyway-core
    - Spring-Security , nimbus-jose-jwt:10.4.2 ,
      OAuth2: _kakao,google_
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
       ├─security/
       │  ├─config/
       │  │      AuthenticationFilter.java
       │  │      CorsConfig.java
       │  │      SecurityConfig.java
       │  │      RSAkeyConfig.java
       │  │
       │  ├─controller/
       │  │      AuthController.java
       │  │      TokenPair.java
       │  │
       │  ├─dto/
       │  │      OIDCUser.java
       │  │      TokenPair.java
       │  │      TokenProperties.java
       │  │
       │  ├─service/
       │  │      AuthService.java
       │  │      SignService.java
       │  │
       │  └─util/
       │         ForensicUtil.java
       │
       ├─user/
       │  ├─controller/
       │  │    UserController.java
       │  │
       │  ├─dto/
       │  │    UserDto.java
       │  │
       │  ├─entity/
       │  │    Role.java
       │  │    User.java
       │  │
       │  ├─repository/
       │  │    UserRepository.java
       │  │
       │  └─service/
       │       UserService.java
       │
       └─PofolitBeApplication.java
  
   ```
