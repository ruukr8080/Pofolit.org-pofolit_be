package com.app.backend.domain.repository;

import com.app.backend.domain.entity.User;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository {
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    Optional<User> findById(UUID id);

    User save(User user);
}
