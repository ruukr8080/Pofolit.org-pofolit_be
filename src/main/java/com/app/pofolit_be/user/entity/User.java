package com.app.pofolit_be.user.entity;

import com.app.pofolit_be.user.dto.OAuth2UserDto;
import com.app.pofolit_be.user.dto.SignupRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.List;
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
   @GeneratedValue(generator = "UUID")
   @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
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
   @ElementCollection(fetch = FetchType.LAZY)
   @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
   @Column(name = "interests_id")
   private List<String> interests;

   @Enumerated(EnumType.STRING)
   private Role role;

   private String refreshToken;

   @Builder
   public User(String email, String nickname, String profileImageUrl, String providerId, String registrationId, Role role) {
      this.email = email;
      this.nickname = nickname;
      this.profileImageUrl = profileImageUrl;
      this.providerId = providerId;
      this.registrationId = registrationId;
      this.role = role;
   }

   public void updateSocialProfile(User user) {
      this.nickname = user.nickname;
      this.profileImageUrl = user.profileImageUrl;
//      return this;
   }

   public void updateRefreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
   }

   public void signup(SignupRequest request) {
      this.nickname = request.nickname();
      this.domain = request.domain();
      this.job = request.job();
      this.interests = request.interests();
      this.role = Role.USER;
   }
}
