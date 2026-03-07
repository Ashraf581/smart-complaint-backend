package com.smartcomplaints.complaint.service;

import com.smartcomplaints.complaint.dto.AuthResponse;
import com.smartcomplaints.complaint.dto.LoginRequest;
import com.smartcomplaints.complaint.dto.RegisterRequest;
import com.smartcomplaints.complaint.entity.User;
import com.smartcomplaints.complaint.repository.UserRepository;
import com.smartcomplaints.complaint.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ============================================
    // Register new user
    // ============================================
    public AuthResponse register(RegisterRequest request) {

        // Check if username already exists
        if (userRepository.existsByUsername(
                request.getUsername())) {
            throw new RuntimeException(
                    "Username already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered!");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Encrypt password before saving!
        user.setPassword(passwordEncoder.encode(
                request.getPassword()));

        // Set role (default CITIZEN if not specified)
        if (request.getRole() != null &&
                request.getRole().equals("ADMIN")) {
            user.setRole(User.Role.ADMIN);
        } else {
            user.setRole(User.Role.CITIZEN);
        }

        userRepository.save(user);
        log.info("✅ New user registered: {} ({})",
                user.getUsername(), user.getRole());

        // Generate token for immediate login
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                "Registration successful!"
        );
    }

    // ============================================
    // Login user
    // ============================================
    public AuthResponse login(LoginRequest request) {

        // Find user by username
        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(
                        "Invalid username or password!"));

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new RuntimeException(
                    "Invalid username or password!");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        log.info("✅ User logged in: {} ({})",
                user.getUsername(), user.getRole());

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                "Login successful!"
        );
    }
}