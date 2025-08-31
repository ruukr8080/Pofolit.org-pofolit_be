package com.app.pofolit_be.user.mapper;

import com.app.pofolit_be.user.dto.SignDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuth2AttributeMapper {

    /**
     * API 제공사명(registrationId)과 사용자 정보(attributes)을 기반으로 SignDto 객체를 생성합니다.
     *
     * @param registrationId OAuth2.0 api 제공사 ID ("google", "kakao")
     * @param attributes 사용자 정보
     * @return 가공 된 SignDto 객체
     */
    public SignDto getOAuth2UserAttribute(String registrationId, Map<String, Object> attributes, String oauthRefreshToken) {
        String email, nickname, profileImageUrl;
        String providerId = String.valueOf(attributes.get("sub"));

        switch (registrationId) {
            case "google":
                email = (String) attributes.get("email");
                nickname = (String) attributes.get("name");
                profileImageUrl = (String) attributes.get("picture");
                break;
            case "kakao":
                nickname = (String) attributes.get("nickname");
                profileImageUrl = (String) attributes.get("picture");
                email = (String) attributes.get("email");
                break;
            default:
                throw new IllegalArgumentException(registrationId);
        }

        return new SignDto(email, nickname, profileImageUrl, registrationId, providerId, oauthRefreshToken);
    }
}