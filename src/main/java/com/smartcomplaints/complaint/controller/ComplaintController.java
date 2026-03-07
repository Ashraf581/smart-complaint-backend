package com.smartcomplaints.complaint.controller;

import com.smartcomplaints.complaint.entity.Complaint;
import com.smartcomplaints.complaint.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping("/complaint")
    public ResponseEntity<Complaint> submitComplaint(
            @RequestBody Complaint complaint,
            Authentication auth) {
        String username = auth.getName();
        Complaint saved = complaintService
                .submitComplaint(complaint, username);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getComplaints(
            Authentication auth) {
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMIN"));

        List<Complaint> complaints;

        if (isAdmin) {
            complaints = complaintService.getAllComplaints();
            log.info("👨‍💼 Admin {} fetching ALL complaints",
                    username);
        } else {
            complaints = complaintService
                    .getMyComplaints(username);
            log.info("👤 Citizen {} fetching own complaints",
                    username);
        }

        return ResponseEntity.ok(complaints);
    }

    @PutMapping("/complaint/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            Complaint updated = complaintService
                    .updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/complaint/{id}/reclassify")
    public ResponseEntity<?> reclassify(
            @PathVariable Long id) {
        try {
            Complaint updated = complaintService
                    .reclassify(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/complaint/{id}")
    public ResponseEntity<?> deleteComplaint(
            @PathVariable Long id) {
        try {
            complaintService.deleteComplaint(id);
            return ResponseEntity.ok(
                    Map.of("message",
                            "Complaint deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}