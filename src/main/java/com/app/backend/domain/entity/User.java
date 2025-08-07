package com.app.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;


@NoArgsConstructor
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
public class User {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String nickname;
    private String email;
    private String profileImage;
    private String provider;
    private String providerId;
    @Enumerated(EnumType.STRING)
    private Role role;

    public User update(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = Role.AUTHOR;
        return this;
    }
    public String getRoleKey() {
        return this.role.getKey();
    }

}
