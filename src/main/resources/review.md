
### 2.1 OAuth2 + JWT 하이브리드 인증 구조

#### 전체 인증 플로우
```
1. 사용자 OAuth2 로그인 요청 (Google/Kakao)
2. OAuth2 Provider 인증 완료
3. AuthSuccessHandler에서 JWT 토큰 생성
4. 토큰을 쿠키에 저장하여 프론트엔드로 리다이렉트
5. 이후 요청시 JwtFilter에서 토큰 검증

6. OIDCUserService에서 사용자 정보 로드
```

#### 👍 잘된 점
- **보안성**: JWT + 쿠키 조합으로 XSS/CSRF 공격에 대한 기본 방어
- **확장성**: OAuth2와 JWT를 분리하여 다양한 Provider 지원 가능
- **토큰 관리**: Access Token과 Refresh Token 분리

#### ⚠️ 심각한 보안 이슈들


**문제점**: 
- `sub` 파라미터로 토큰 타입을 구분하는 것은 매우 위험
- `sub`는 JWT 표준에서 사용자 식별자를 의미하므로 잘못된 사용
- 실제 사용자 ID가 "refreshToken"일 경우 잘못된 TTL 적용 가능


##### 2.1.2 토큰 저장 방식의 보안 취약점
```java
// AuthSuccessHandler.java 52-53행
cookieUtil.addCookie(response, "pre", accessToken);  // 위험!
cookieUtil.addCookie(response,"refreshToken", refreshToken);
```

**문제점**:
- Access Token을 "pre"라는 불분명한 이름으로 저장
- HttpOnly, Secure, SameSite 설정 확인 필요
- 토큰이 평문으로 쿠키에 저장


### 2.2 사용자 서비스 구조

#### 👍 잘된 점
- **OIDCUserService**: OAuth2 사용자 정보를 적절히 변환
- **UserPrincipal 구조**: Spring Security와 잘 통합됨

#### ⚠️ 개선 필요사항
- **UserService와 SignService 분리**: 역할이 불분명하게 중복됨
- **사용자 상태 관리**: 회원가입 완료 여부 등의 상태 관리 부족

## 3. 코드 품질 및 에러 처리

### 3.1 예외 처리 체계

#### 👍 잘된 점
- **GlobalExceptionHandler**: 중앙집중식 예외 처리 구조 우수
- **커스텀 예외**: `CustomException`, `RequestCookieException` 등 목적별 예외 클래스
- **ExCode 열거형**: 예외 코드와 메시지를 체계적으로 관리

#### ⚠️ 개선 필요사항

##### 3.1.1 예외 처리 누락
```java
// JwtFilter.java 84-87행
} catch (Exception e) {
    log.warn("\nJWT 필터에서 인증 처리 중 오류 발생: {}", e.getMessage());
    SecurityContextHolder.clearContext();
}
```

**문제점**: 
- 모든 예외를 `Exception`으로 처리하여 구체적인 에러 대응 불가
- 에러 로그만 남기고 사용자에게 적절한 응답 없음

**개선안**:
```java
} catch (JwtException e) {
    log.warn("JWT 토큰 오류: {}", e.getMessage());
    // 토큰 오류 응답 처리
} catch (UsernameNotFoundException e) {
    log.warn("사용자 정보 없음: {}", e.getMessage());
    // 사용자 정보 오류 응답 처리
} catch (Exception e) {
    log.error("예상치 못한 인증 오류", e);
    // 일반 오류 응답 처리
}
```

##### 3.1.2 Redis 에러 처리 부족
```java
// ExCode.java에는 Redis 관련 예외 코드가 정의되어 있으나
// 실제 Redis 사용 코드에서 예외 처리 로직이 부족
```

### 3.2 로깅 체계

#### 👍 잘된 점
- **Slf4j 사용**: 표준 로깅 프레임워크 사용
- **구조화된 로깅**: 대부분의 로그가 의미 있는 정보를 담고 있음

#### ⚠️ 개선 필요사항
- **로그 레벨 일관성**: info, warn, error 레벨이 혼재됨
- **민감 정보 로깅**: 토큰 값이 로그에 노출될 수 있음
```java
// JwtFilter.java 101행
log.info("요청헤더에 Authorization\n[{}]", bearerToken); // 토큰 노출 위험
```

### 3.3 코드 품질 이슈


#### 3.3.3 불완전한 구현
```java
// AuthController.java 57-62행
@PostMapping("/logout")
public ResponseEntity<Void> logout(HttpServletResponse response,
                                   @CookieValue(value = "refreshToken", required = false) String refreshToken) {
    return ResponseEntity.ok().build(); // 실제 로그아웃 로직 미구현
}
```

## 4. 전체적인 권장사항

### 4.1 즉시 수정 필요 (보안 이슈)
1. **JWT TTL 로직 수정**: 토큰 타입별 명확한 TTL 관리
2. **토큰 검증 로직 강화**: Access/Refresh Token 구분 사용
3. **쿠키 보안 설정 확인**: HttpOnly, Secure, SameSite 설정
4. **로그아웃 기능 완성**: 실제 토큰 무효화 로직 구현

### 4.2 구조적 개선사항
2. **서비스 계층 역할 명확화**: UserService vs SignService 구분
3. **예외 처리 체계화**: 구체적인 예외 타입별 처리 로직
4. **설정 외부화**: 하드코딩된 값들을 application.yml로 이동

### 4.3 코드 품질 향상
1. **테스트 코드 추가**: 특히 보안 관련 로직의 단위 테스트
2. **문서화 개선**: API 문서 및 코드 주석 보강
3. **코드 일관성**: 네이밍 컨벤션 통일
4. **리팩토링**: 중복 코드 제거 및 메서드 분리

### 4.4 모니터링 및 운영
1. **로깅 체계 개선**: 민감정보 제외, 레벨별 일관성
2. **헬스체크 확장**: 데이터베이스, Redis 연결 상태 포함
3. **메트릭스 추가**: 인증 실패율, 토큰 발급 현황 등

## 결론

프로젝트는 전반적으로 Spring Boot와 Spring Security의 표준 구조를 잘 따르고 있으나, 
**보안 관련 로직에서 심각한 취약점**이 발견되었습니다. 
특히 JWT 토큰 관리 부분은 즉시 수정이 필요합니다. 

코드 품질 측면에서는 일관성 있는 네이밍과 예외 처리 체계 개선이 필요하며, 불완전한 구현들을 완성해야 합니다.

**우선순위**: 보안 이슈 수정 → 구조적 개선 → 코드 품질 향상 순으로 진행하는 것을 권장합니다.