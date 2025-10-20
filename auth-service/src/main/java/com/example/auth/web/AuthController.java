package com.example.auth.web;

import com.example.auth.entity.User;
import com.example.auth.repo.UserRepository;
import com.example.auth.service.InvalidCredentialsException;
import com.example.auth.service.TokenService;
import com.example.auth.service.UserService;
import com.example.auth.web.dto.RegisterRequest;
import com.example.auth.web.dto.RegisterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(UserService users,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          TokenService tokenService) {
        this.users = users;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
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

    // ---- LOGIN: returns token in header + body
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        User u = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = tokenService.issue(u);
        return ResponseEntity.ok()
                .header("X-Auth-Token", token)
                .body(new LoginResponse(u.getId(), u.getUsername(), token, "Login successful"));
    }

    // ---- INTROSPECT: used by Gateway to validate token
    @GetMapping("/introspect")
    public ResponseEntity<Map<String, Object>> introspect(@RequestHeader("X-Auth-Token") String token) {
        boolean active = tokenService.isActive(token);
        int code = active ? 200 : 401;
        return ResponseEntity.status(code).body(Map.of("active", active));
    }

    // ---- LOGOUT: revoke token (idempotent)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("X-Auth-Token") String token) {
        tokenService.revoke(token);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // DTOs for this controller
    public record LoginRequest(String username, String password) {}
    public record LoginResponse(Long userId, String username, String token, String message) {}
}
