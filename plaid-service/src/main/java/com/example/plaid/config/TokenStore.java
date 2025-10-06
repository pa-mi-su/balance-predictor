package com.example.plaid.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final ConcurrentHashMap<Long, String> map = new ConcurrentHashMap<>();
    public void put(Long userId, String accessToken) { map.put(userId, accessToken); }
    public String get(Long userId) { return map.get(userId); }
}
