package com.app.pofolit_be.mapper.provider;

import com.app.pofolit_be.dto.UserDto;

import java.util.Map;

public interface OAuth2AttributeMapper {
   UserDto mapToDto(Map<String, Object> attributes);

   boolean supports(String registrationId);
}