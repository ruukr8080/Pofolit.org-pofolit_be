package com.app.pofolit_be.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//@NoArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_openid",
                        columnNames = {"provider", "subject"}
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
    private String avatar; // 카카오:110px*110px
    private String provider; // 구글,카카오
    private String subject;
    @Getter
    @Enumerated(EnumType.STRING)
    private Role access;

    @Builder
    public User(String email, String nickname, String avatar,
                String provider, String subject, Role access) {
        this.email = email;
        this.nickname = nickname;
        this.avatar = avatar;
        this.provider = provider;
        this.subject = subject;
        this.access = access != null ? access : Role.LV0;
    }

    public void completeSignup(String nickname, String avatar) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.access = Role.LV1;
    }

    public void updateProfile(String nickname, String avatar) {
        this.nickname = nickname;
        this.avatar = avatar;
    }
}