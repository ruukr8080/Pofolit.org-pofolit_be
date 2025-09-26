package com.app.pofolit_be.user.repository;

import com.app.pofolit_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndSubject(String provider, String subject);
}
