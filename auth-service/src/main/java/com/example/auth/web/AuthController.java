package com.example.auth.web;

import com.example.auth.jwt.JwtService;
import com.example.auth.user.User;
import com.example.auth.user.UserRepository;
import com.example.auth.web.dto.AuthDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UserRepository users, BCryptPasswordEncoder encoder, JwtService jwtService) {
        this.users = users; this.encoder = encoder; this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> register(@RequestBody RegisterRequest req) {
        users.findByEmail(req.email()).ifPresent(u -> { throw new IllegalArgumentException("Email exists"); });
        var u = new User();
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        users.save(u);
        return Map.of("id", u.getId(), "email", u.getEmail());
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) throws Exception {
        var user = users.findByEmail(req.email()).orElseThrow(() -> new IllegalArgumentException("Bad credentials"));
        if (!encoder.matches(req.password(), user.getPasswordHash())) throw new IllegalArgumentException("Bad credentials");
        var jwt = jwtService.issueAccessToken(user.getId(), user.getEmail());
        return new TokenResponse(jwt, "Bearer");
    }
}
