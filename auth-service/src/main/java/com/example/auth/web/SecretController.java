package com.example.auth.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
class SecretController {
    @GetMapping("/api/auth/secret")
    public Map<String, String> secret() {
        return Map.of("secret", "shh");
    }
}
