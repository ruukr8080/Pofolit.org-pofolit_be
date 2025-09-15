package com.app.pofolit_be.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

/**
 * jjwt 0.12.6
 * JwtHeaderProperties record
 * JwtHeader
 * JwtPayloadProperties record
 * JwtPayloadMapper
 * JwtDigest
 * signature
 * JwtParser
 * JWT 정적 설정 값을 바인딩합니다.
 * iss: 토큰 발급자 (issuer)
 * sub: 토큰 제목 (subject)
 * aud: 토큰 대상자 (audience)
 * exp: 토큰의 만료시간 (expiraton), 시간은 NumericDate 형식으로 되어있어야 하며 (예: 1480849147370)
 * 언제나 현재 시간보다 이후로 설정되어있어야합니다.
 * nbf: Not Before 를 의미하며, 토큰의 활성 날짜와 비슷한 개념입니다.
 * 여기에도 NumericDate 형식으로 날짜를 지정하며, 이 날짜가 지나기 전까지는 토큰이 처리되지 않습니다.
 * iat: 토큰이 발급된 시간 (issued at), 이 값을 사용하여 토큰의 age 가 얼마나 되었는지 판단 할 수 있습니다.
 * jti: JWT의 고유 식별자로서, 주로 중복적인 처리를 방지하기 위하여 사용됩니다. 일회용 토큰에 사용하면 유용합니다.
 * <p>
 * param algorithm "sign with algorithm" ${SECRET_KEY} ("arg":"HS512")
 * param `keySize` keySize
 * param `iss` "pofolit"
 */
@Slf4j
@ConfigurationProperties(prefix = "spring.jwt")
public record JwtProperties(
        int keySize,
        String iss,
        long pTtl,
        long aTtl,
        long rTtl
)
{
    @Bean
    public KeyPair rsaKeyPair() {
        Instant start = Instant.now();
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize);
            KeyPair keyPair = keyGen.generateKeyPair();
            Instant end = Instant.now();
            log.info("RSA KeyPair | 암호키 bit : {} \n생성하는데 걸린시간: {}ms"
                    , keySize, Duration.between(start, end).toMillis());
            return keyPair;

        } catch (NoSuchAlgorithmException e) {
            log.error("RSA KeyPair \n 암호키 bit: [{}] ", keySize);
            throw new IllegalStateException("암호키 알고리즘 변경해야 합니다.", e);
        }
    }
}