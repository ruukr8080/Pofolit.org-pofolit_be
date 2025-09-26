package com.app.pofolit_be.user.service;

import com.app.pofolit_be.user.dto.UserDto;
import com.app.pofolit_be.user.entity.User;
import com.app.pofolit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User getUserByProvider(String provider, String subject) {
        return userRepository.findByProviderAndSubject(provider, subject)
                .orElse(null);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User createUser(UserDto dto) {
        User newUser = dto.toUser();
        return userRepository.save(newUser);
    }

    @Transactional
    public User completeSignup(User user, UserDto dto) {
        dto.signupUser(user);
        return user;
    }

    @Transactional
    public User updateProfile(User user, UserDto dto) {
        dto.updateUser(user);
        return user;
    }
}
