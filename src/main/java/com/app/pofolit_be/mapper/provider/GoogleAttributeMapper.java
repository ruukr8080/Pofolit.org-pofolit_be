package com.app.pofolit_be.mapper.provider;

import com.app.pofolit_be.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleAttributeMapper implements OAuth2AttributeMapper {

   @Override
   public UserDto mapToDto(Map<String, Object> attributes) {
      return new UserDto(
              (String) attributes.get("email"),
              (String) attributes.get("name"),
              (String) attributes.get("picture"),
              (String) attributes.get("sub")
      );
   }

   @Override
   public boolean supports(String registrationId) {
      return "google".equalsIgnoreCase(registrationId);
   }
}