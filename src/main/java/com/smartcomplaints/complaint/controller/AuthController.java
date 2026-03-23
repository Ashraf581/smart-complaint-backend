package com.smartcomplaints.complaint.controller;

import com.smartcomplaints.complaint.dto.AuthResponse;
import com.smartcomplaints.complaint.dto.LoginRequest;
import com.smartcomplaints.complaint.dto.RegisterRequest;
import com.smartcomplaints.complaint.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j  // ← ADD THIS to enable logging
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        long startTime = System.currentTimeMillis();
        log.info("📝 [REGISTER] New registration attempt: {}", request.getUsername());

        try {
            AuthResponse response = authService.register(request);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ [REGISTER] User created successfully: {} (took {}ms)",
                    request.getUsername(), duration);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("❌ [REGISTER] Failed for {}: {} (took {}ms)",
                    request.getUsername(), e.getMessage(), duration);

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        long startTime = System.currentTimeMillis();
        log.info("🔐 [LOGIN] Attempt for user: {}", request.getUsername());

        try {
            AuthResponse response = authService.login(request);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ [LOGIN] Successful for user: {} (took {}ms)",
                    request.getUsername(), duration);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("❌ [LOGIN] Failed for {}: {} (took {}ms)",
                    request.getUsername(), e.getMessage(), duration);

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}