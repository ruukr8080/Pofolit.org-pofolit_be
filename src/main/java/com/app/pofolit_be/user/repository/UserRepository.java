package com.app.pofolit_be.user.repository;

import com.app.pofolit_be.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findBySubject(String subject);
}
