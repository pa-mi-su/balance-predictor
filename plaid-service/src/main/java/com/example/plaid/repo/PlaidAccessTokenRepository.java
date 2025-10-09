package com.example.plaid.repo;

import com.example.plaid.entity.PlaidAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaidAccessTokenRepository extends JpaRepository<PlaidAccessToken, Long> {
    Optional<PlaidAccessToken> findByUserId(Long userId);
}
