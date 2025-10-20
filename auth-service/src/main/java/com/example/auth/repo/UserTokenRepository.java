package com.example.auth.repo;

import com.example.auth.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByToken(String token);

    @Query("""
           select ut from UserToken ut
           where ut.token = :token
             and ut.revoked = false
             and ut.expiresAt > :now
           """)
    Optional<UserToken> findActiveByToken(String token, Instant now);

    @Modifying
    @Query("update UserToken ut set ut.revoked = true where ut.token = :token")
    int revokeByToken(String token);

    @Modifying
    @Query("update UserToken ut set ut.revoked = true where ut.user.id = :userId and ut.revoked = false")
    int revokeAllForUser(Long userId);
}
