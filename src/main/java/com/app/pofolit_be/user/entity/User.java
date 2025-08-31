package com.app.pofolit_be.user.entity;

import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.dto.SignupRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_provider",
                        columnNames = {"registration_id", "provider_id"}
                )
        })
@Entity
public class User {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(unique = true)
    private String email;
    private String nickname;
    @Column(name = "profile_image_url", length = 355)
    private String profileImageUrl; // 카카오:110px*110px

    private String providerId;
    private String registrationId;
    private LocalDate birthDay;
    private String job;
    private String domain;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String jwtRefreshToken;
    private String oauthRefreshToken;

    @Builder
    public User(UUID id, String email, String nickname, String jwtRefreshToken, String profileImageUrl, String providerId, String registrationId, Role role, String oauthRefreshToken) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.providerId = providerId;
        this.registrationId = registrationId;
        this.role = role;
        this.oauthRefreshToken = oauthRefreshToken;
    }

    public void update(SignDto signDto) {
        this.nickname = signDto.nickname();
        this.profileImageUrl = signDto.profileImageUrl();
        if(signDto.refreshToken() != null)
            this.oauthRefreshToken = signDto.refreshToken();
    }

    public void updateRefreshToken(String jwtRefreshToken) {
        this.jwtRefreshToken = jwtRefreshToken;
    }

    public void signup(SignupRequest request) {
        this.nickname = request.nickname();
        this.birthDay = request.birthDay();
        this.domain = request.domain();
        this.job = request.job();
        this.role = Role.USER;
    }
}
