package com.app.pofolit_be.user.dto;

import com.app.pofolit_be.user.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record UserDetailsDto(
        String aka,
        @JsonProperty("birthDay")
        LocalDate birthDay,
        String domain,
        String job,
        List<String> interests,
        Role role
)
{

}

