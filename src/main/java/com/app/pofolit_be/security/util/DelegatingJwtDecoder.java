package com.app.pofolit_be.security.util;

import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.text.ParseException;
import java.util.Map;

@RequiredArgsConstructor
public class DelegatingJwtDecoder implements JwtDecoder {

    private final Map<String, JwtDecoder> decoders;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            String algorithm = JWTParser.parse(token).getHeader().getAlgorithm().getName();
            JwtDecoder decoder = decoders.get(algorithm);

            if(decoder == null) {
                throw new BadJwtException("지원하지 않는 JWT 알고리즘입니다: " + algorithm);
            }
            return decoder.decode(token);
        } catch (ParseException e) {
            throw new BadJwtException("JWT 파싱 실패", e);
        }
    }
}