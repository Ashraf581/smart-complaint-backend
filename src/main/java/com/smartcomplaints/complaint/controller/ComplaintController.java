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

        long startTime = System.currentTimeMillis();
        String username = auth.getName();

        log.info("📝 [API] POST /complaint - User: {}, Title: {}",
                username, complaint.getTitle());

        try {
            Complaint saved = complaintService.submitComplaint(complaint, username);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ [API] Complaint #{} submitted by {} (took {}ms)",
                    saved.getId(), username, duration);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("❌ [API] Failed to submit complaint for {}: {}",
                    username, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getComplaints(
            Authentication auth) {

        long startTime = System.currentTimeMillis();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMIN"));

        List<Complaint> complaints;

        if (isAdmin) {
            log.info("👨‍💼 [API] GET /complaints - Admin {} fetching ALL complaints", username);
            complaints = complaintService.getAllComplaints();
        } else {
            log.info("👤 [API] GET /complaints - Citizen {} fetching own complaints", username);
            complaints = complaintService.getMyComplaints(username);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ [API] Retrieved {} complaints for {} (took {}ms)",
                complaints.size(), username, duration);

        return ResponseEntity.ok(complaints);
    }

    @PutMapping("/complaint/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        long startTime = System.currentTimeMillis();
        String status = body.get("status");

        log.info("🔄 [API] PUT /complaint/{}/status - New status: {}", id, status);

        try {
            Complaint updated = complaintService.updateStatus(id, status);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ [API] Complaint #{} status updated to {} (took {}ms)",
                    id, status, duration);

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            log.warn("❌ [API] Failed to update complaint #{} status: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/complaint/{id}/reclassify")
    public ResponseEntity<?> reclassify(
            @PathVariable Long id) {

        long startTime = System.currentTimeMillis();
        log.info("🤖 [API] PUT /complaint/{}/reclassify - Request received", id);

        try {
            Complaint updated = complaintService.reclassify(id);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ [API] Complaint #{} reclassified to {} (took {}ms)",
                    id, updated.getDepartment(), duration);

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            log.warn("❌ [API] Failed to reclassify complaint #{}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/complaint/{id}")
    public ResponseEntity<?> deleteComplaint(
            @PathVariable Long id) {

        long startTime = System.currentTimeMillis();
        log.info("🗑️ [API] DELETE /complaint/{} - Delete request", id);

        try {
            complaintService.deleteComplaint(id);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ [API] Complaint #{} deleted successfully (took {}ms)", id, duration);

            return ResponseEntity.ok(Map.of("message", "Complaint deleted successfully!"));

        } catch (RuntimeException e) {
            log.warn("❌ [API] Failed to delete complaint #{}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}