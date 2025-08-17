package com.app.pofolit_be.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity(name = "users")
public class User {

   @Id
   @GeneratedValue(generator = "UUID")
   @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
   @Column(columnDefinition = "BINARY(16)")
   private UUID id;

   @Column(unique = true)
   private String email;
   private String nickname;
   private String profileImageUrl;

   private String providerId;
   private String registrationId;

   private String refreshToken;

   @Enumerated(EnumType.STRING)
   private Role role;

   public User update(String nickname, String profileImageUrl) {
      this.nickname = nickname;
      this.profileImageUrl = profileImageUrl;
      return this;
   }
}
