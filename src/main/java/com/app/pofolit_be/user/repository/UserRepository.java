package com.app.pofolit_be.user.repository;

import com.app.pofolit_be.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

   Optional<User> findByRegistrationIdAndProviderId(String registrationId, String providerId);

   Optional<User> findUserByEmail(String email);
}
