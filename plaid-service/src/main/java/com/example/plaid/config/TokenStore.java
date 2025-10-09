package com.example.plaid.config;

import com.example.plaid.entity.PlaidAccessToken;
import com.example.plaid.repo.PlaidAccessTokenRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenStore {

    private final PlaidAccessTokenRepository repo;

    public TokenStore(PlaidAccessTokenRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void put(Long userId, String accessToken, String itemId) {
        // Upsert by userId (unique)
        var existing = repo.findByUserId(userId).orElse(null);
        if (existing == null) {
            var entity = new PlaidAccessToken();
            entity.setUserId(userId);
            entity.setAccessToken(accessToken);
            entity.setItemId(itemId);
            try {
                repo.save(entity);
            } catch (DataIntegrityViolationException e) {
                // In case of race, update the one that won
                var winner = repo.findByUserId(userId).orElseThrow();
                winner.setAccessToken(accessToken);
                winner.setItemId(itemId);
                repo.save(winner);
            }
        } else {
            existing.setAccessToken(accessToken);
            existing.setItemId(itemId);
            repo.save(existing);
        }
    }

    @Transactional(readOnly = true)
    public String get(Long userId) {
        return repo.findByUserId(userId).map(PlaidAccessToken::getAccessToken).orElse(null);
    }

    @Transactional(readOnly = true)
    public String getItemId(Long userId) {
        return repo.findByUserId(userId).map(PlaidAccessToken::getItemId).orElse(null);
    }
}
