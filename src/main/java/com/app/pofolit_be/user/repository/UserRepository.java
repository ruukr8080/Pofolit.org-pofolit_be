package com.app.pofolit_be.user.repository;

import com.app.pofolit_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);

    Optional<User> findByRegistrationIdAndProviderId(String registrationId, String providerId);

    @Query("SELECT u FROM User u WHERE (u.registrationId = :regId AND u.providerId = :provId) OR u.email = :email")
    Optional<User> findBySocialOrEmail(@Param("regId") String registrationId,
                                       @Param("provId") String providerId,
                                       @Param("email") String email);

    Optional<User> findByProviderId(String providerId);

    User findUserById(Long id);
}
