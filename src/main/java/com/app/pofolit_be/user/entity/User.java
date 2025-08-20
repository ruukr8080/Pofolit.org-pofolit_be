package com.app.pofolit_be.user.entity;

import com.app.pofolit_be.user.dto.UserDetailsDto;
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
   private String profileImageUrl; // 카카오:110px*110px

   private String providerId;
   private String registrationId;
   private LocalDate birthDay;
   private String job;
   private String domain;
   @ElementCollection(fetch = FetchType.LAZY)
   @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
   @Column(name = "interest")
   private List<String> interests;

   @Enumerated(EnumType.STRING)
   private Role role;

   @Builder
   public User(String email, String nickname, String profileImageUrl, String providerId, String registrationId, Role role) {
      this.email = email;
      this.nickname = nickname;
      this.profileImageUrl = profileImageUrl;
      this.providerId = providerId;
      this.registrationId = registrationId;
      this.role = role;
   }

   public User updateProfile(String nickname, String profileImageUrl) {
      this.nickname = nickname;
      this.profileImageUrl = profileImageUrl;
      return this;
   }

   public void completeRegistration(UserDetailsDto details) {
      this.nickname = details.aka();
      this.birthDay = details.birthDay();
      this.job = details.job();
      this.domain = details.domain();
      this.interests = details.interests();
      this.role = Role.USER;
   }
}
