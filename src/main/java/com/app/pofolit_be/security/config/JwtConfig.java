package com.app.pofolit_be.security.config;

import com.app.pofolit_be.security.dto.TokenProperties;
import com.app.pofolit_be.security.util.DelegatingJwtDecoder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

  private final TokenProperties tokenProperties;

  @Bean
  public RSAPrivateKey rsaPrivateKey() throws Exception {
    try (InputStream is = new ClassPathResource("private_key.pem").getInputStream()) {
      String privateKeyPEM = new String(is.readAllBytes(), StandardCharsets.UTF_8)
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replace("-----END PRIVATE KEY-----", "")
          .replaceAll("\\s", "");
      byte[] decoded = java.util.Base64.getDecoder().decode(privateKeyPEM);
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }
  }

  @Bean
  public RSAPublicKey rsaPublicKey() throws Exception {
    try (InputStream is = new ClassPathResource("public_key.pem").getInputStream()) {
      String publicKeyPEM = new String(is.readAllBytes(), StandardCharsets.UTF_8)
          .replace("-----BEGIN PUBLIC KEY-----", "")
          .replace("-----END PUBLIC KEY-----", "")
          .replaceAll("\\s", "");
      byte[] decoded = java.util.Base64.getDecoder().decode(publicKeyPEM);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) keyFactory.generatePublic(spec);
    }
  }

  @Bean
  @Primary
  public JwtEncoder jwtEncoder(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
    RSAKey rsaKey = new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID("pofolit-temp-key")
        .build();
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  @Primary
  public JwtDecoder jwtDecoder(@Qualifier("rs256Decoder") JwtDecoder rs256Decoder,
      @Qualifier("hs256Decoder") JwtDecoder hs256Decoder) {
    return new DelegatingJwtDecoder(Map.of(
        JWSAlgorithm.RS256.getName(), rs256Decoder,
        MacAlgorithm.HS256.getName(), hs256Decoder
    ));
  }

  @Bean("hs256Decoder")
  public JwtDecoder hs256Decoder() {
    SecretKey secretKey = new SecretKeySpec(tokenProperties.getSecret().getBytes(), "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
  }

  @Bean("rs256Decoder")
  public JwtDecoder rs256Decoder(RSAPublicKey publicKey) {
    return NimbusJwtDecoder.withPublicKey(publicKey).signatureAlgorithm(SignatureAlgorithm.RS256)
        .build();
  }
}