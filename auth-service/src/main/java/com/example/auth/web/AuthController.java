package com.example.auth.web;

import com.example.auth.service.UserService;
import com.example.auth.web.dto.RegisterRequest;
import com.example.auth.web.dto.RegisterResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @PostMapping("/register")
    public RegisterResponse register(@jakarta.validation.Valid @RequestBody RegisterRequest req) {
        var u = users.register(req.username(), req.email(), req.password());
        return new RegisterResponse(u.getId(), u.getUsername(), u.getEmail());
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }
}
