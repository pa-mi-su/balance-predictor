package com.example.auth.service.impl;

import com.example.auth.entity.User;
import com.example.auth.entity.UserToken;
import com.example.auth.repo.UserTokenRepository;
import com.example.auth.service.TokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class TokenServiceImpl implements TokenService {

    private final UserTokenRepository tokens;
    private final SecureRandom rng = new SecureRandom();

    /** Minutes until token expiry (configurable) */
    private final long ttlMinutes;

    public TokenServiceImpl(UserTokenRepository tokens,
                            @Value("${auth.token.ttl-minutes:120}") long ttlMinutes) {
        this.tokens = tokens;
        this.ttlMinutes = ttlMinutes;
    }

    @Override
    @Transactional
    public String issue(User user) {
        // 32 random bytes → URL-safe Base64 (no padding)
        byte[] bytes = new byte[32];
        rng.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        UserToken ut = new UserToken();
        ut.setUser(user);
        ut.setToken(token);
        ut.setTokenType("OPAQUE");
        ut.setRevoked(false);
        ut.setCreatedAt(Instant.now());
        ut.setExpiresAt(Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES));

        tokens.save(ut);
        return token;
    }

    @Override
    public boolean isActive(String token) {
        return tokens.findActiveByToken(token, Instant.now()).isPresent();
    }

    @Override
    @Transactional
    public void revoke(String token) {
        tokens.revokeByToken(token);
    }
}
