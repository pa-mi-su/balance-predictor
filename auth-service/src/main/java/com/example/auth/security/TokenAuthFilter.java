package com.example.auth.security;

import com.example.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.Collections;

/**
 * Simple opaque-token auth using the X-Auth-Token header.
 * If the token is active, we mark the request as authenticated (no authorities).
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public TokenAuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Only proceed if not already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = request.getHeader("X-Auth-Token");

            if (token != null && !token.isBlank() && tokenService.isActive(token)) {
                // Keep it minimal: principal is the token string; no authorities for now.
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        /* principal */ token,
                        /* credentials */ null,
                        /* authorities */ Collections.emptyList()
                );
                ((UsernamePasswordAuthenticationToken) auth)
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
