package com.app.pofolit_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity(name = "users")
public class User {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(unique = true)
   private String email;
   private String nickname;
   private String profileImageUrl;

   @Column(unique = true)
   private String providerId;
   private String provider;

   private String refreshToken;

   @Enumerated(EnumType.STRING)
   private Role role;

   public User update(String nickname, String profileImageUrl) {
      this.nickname = nickname;
      this.profileImageUrl = profileImageUrl;
      return this;
   }
}
