package com.smartcomplaints.complaint.controller;

import com.smartcomplaints.complaint.dto.AuthResponse;
import com.smartcomplaints.complaint.dto.LoginRequest;
import com.smartcomplaints.complaint.dto.RegisterRequest;
import com.smartcomplaints.complaint.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {
        try {
            AuthResponse response =
                    authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {
        try {
            AuthResponse response =
                    authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}