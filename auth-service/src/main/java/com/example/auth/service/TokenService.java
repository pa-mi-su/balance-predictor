package com.example.auth.service;

import com.example.auth.entity.User;

public interface TokenService {
    /** Issues a new opaque token for the user, persists it, and returns the raw token string. */
    String issue(User user);

    /** Returns true if the token exists, not revoked, and not expired. */
    boolean isActive(String token);

    /** Revokes the token (idempotent). */
    void revoke(String token);
}
