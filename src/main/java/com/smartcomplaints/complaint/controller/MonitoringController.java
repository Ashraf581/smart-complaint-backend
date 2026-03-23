package com.smartcomplaints.complaint.controller;

import com.smartcomplaints.complaint.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class MonitoringController {

    private final ComplaintRepository complaintRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Backend is running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<com.smartcomplaints.complaint.entity.Complaint> allComplaints = complaintRepository.findAll();
            long total = allComplaints.size();

            long pending = allComplaints.stream()
                    .filter(c -> "PENDING".equals(c.getStatus()))
                    .count();

            long resolved = allComplaints.stream()
                    .filter(c -> "RESOLVED".equals(c.getStatus()))
                    .count();

            long verified = allComplaints.stream()
                    .filter(c -> "VERIFIED".equals(c.getVerificationStatus()))
                    .count();

            stats.put("totalComplaints", total);
            stats.put("pending", pending);
            stats.put("resolved", resolved);
            stats.put("verified", verified);
            stats.put("timestamp", LocalDateTime.now().toString());

        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }

        return ResponseEntity.ok(stats);
    }
}