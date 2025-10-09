package com.example.auth.web;

import com.example.auth.jwt.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwksController {
    private final JwtService jwtService;
    public JwksController(JwtService jwtService) { this.jwtService = jwtService; }

    @GetMapping("/oauth2/jwks")
    public Map<String, Object> jwks() {
        return jwtService.jwks();
    }
}
