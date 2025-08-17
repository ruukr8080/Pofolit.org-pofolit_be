package com.app.pofolit_be.user.dto.converter;

import com.app.pofolit_be.user.dto.OAuth2UserDto;

import java.util.Map;

public interface OAuth2Converter {
   OAuth2UserDto convertToDto(Map<String, Object> attributes, String registrationId);
}