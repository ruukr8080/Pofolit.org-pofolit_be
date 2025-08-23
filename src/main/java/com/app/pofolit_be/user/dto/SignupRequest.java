package com.app.pofolit_be.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/*
 * Sign up RequestDto
 * */
public record SignupRequest(
        String nickname,
        @JsonProperty("birth_day")
        LocalDate birthDay,
        String domain,
        String job,
        List<String> interests
)
{
}

