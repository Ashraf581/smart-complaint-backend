package com.smartcomplaints.complaint.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// ============================================
// JwtFilter.java
// Intercepts every request and checks JWT token
// ============================================

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Get Authorization header
        String authHeader = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (authHeader != null &&
                authHeader.startsWith("Bearer ")) {

            // Extract token (remove "Bearer " prefix)
            String token = authHeader.substring(7);

            // Validate token
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                // Set authentication in Spring Security context
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority(
                                        "ROLE_" + role))
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(auth);
            }
        }

        // Continue with request
        filterChain.doFilter(request, response);
    }
}