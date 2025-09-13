package com.app.pofolit_be.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * SignupRequestDto
 *
 * @param nickname
 * @param birthDay
 * @param profileImageUrl
 * @param domain
 * @param job
 */
public record SignupRequest(
        String nickname,
        @JsonProperty("birthDay")
        LocalDate birthDay,
        @JsonProperty("profileImageUrl")
        String profileImageUrl,
        String domain,
        String job
)
{ }