package com.app.pofolit_be.user.entity;

import com.app.pofolit_be.security.SecurityLevel;
import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.dto.SignupRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    private String nickname;
    @Column(name = "profile_image_url", length = 355)
    private String profileImageUrl; // 카카오:110px*110px
    private LocalDate birthDay;
    private String job;
    private String domain;

    private String providerId;
    private String registrationId;

//    @Setter
//    @Enumerated(EnumType.STRING)
//    private Role role;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private SecurityLevel securityLevel;


    @Builder
    public User(String email, String nickname, String profileImageUrl, String providerId,
                String registrationId, SecurityLevel securityLevel) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.providerId = providerId;
        this.registrationId = registrationId;
        this.securityLevel = securityLevel;
    }

    public void updateUser(SignDto signDto) {
        this.nickname = signDto.nickname();
        this.profileImageUrl = signDto.profileImageUrl();
    }

    public void signup(SignupRequest request) {
        this.nickname = request.nickname();
        this.birthDay = request.birthDay();
        this.domain = request.domain();
        this.job = request.job();
        this.securityLevel = SecurityLevel.fromLv(securityLevel.getLv());
    }
}