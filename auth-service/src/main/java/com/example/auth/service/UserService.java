package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     * Assumes request has already been validated by the controller (@Valid).
     * Throws DuplicateUserException for username/email conflicts (handled by ControllerAdvice -> 409).
     */
    @Transactional
    public User register(String username, String email, String rawPassword) {
        // Basic normalization (validation should already ensure non-blank)
        final String uname = username.trim();
        final String mail  = email.trim().toLowerCase(Locale.ROOT);
        final String pass  = rawPassword; // do not trim passwords

        // Conflict checks
        users.findByUsernameIgnoreCase(uname).ifPresent(u -> {
            throw new DuplicateUserException("Username '" + uname + "' is already taken.");
        });
        users.findByEmailIgnoreCase(mail).ifPresent(u -> {
            throw new DuplicateUserException("Email '" + mail + "' is already registered.");
        });

        // Hash & persist
        final String hash = passwordEncoder.encode(pass);

        return users.save(
                User.builder()
                        .username(uname)
                        .email(mail)
                        .passwordHash(hash)
                        .build()
        );
    }
}
