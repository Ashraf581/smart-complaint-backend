package com.smartcomplaints.complaint.service;

import com.smartcomplaints.complaint.entity.Complaint;
import com.smartcomplaints.complaint.repository.ComplaintRepository;
import com.smartcomplaints.complaint.service.client.MlServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final MlServiceClient mlServiceClient;

    // ============================================
    // Submit new complaint — saves username!
    // ============================================
    public Complaint submitComplaint(
            Complaint complaint, String username) {

        log.info("📝 New complaint from {}: {}",
                username, complaint.getTitle());

        // Save who submitted this complaint
        complaint.setUsername(username);

        // Call ML service for prediction
        MlServiceClient.MlResponse prediction =
                mlServiceClient.predict(
                        complaint.getTitle(),
                        complaint.getDescription());

        complaint.setDepartment(prediction.getDepartment());
        complaint.setPriority(prediction.getPriority());
        complaint.setConfidence(prediction.getConfidence());

        Complaint saved = complaintRepository.save(complaint);

        log.info("✅ Saved! ID={}, Dept={}, Priority={}",
                saved.getId(),
                saved.getDepartment(),
                saved.getPriority());

        return saved;
    }

    // ============================================
    // Get ALL complaints (Admin only)
    // ============================================
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    // ============================================
    // Get complaints for CITIZEN (their own only)
    // ============================================
    public List<Complaint> getMyComplaints(String username) {
        log.info("📋 Getting complaints for: {}", username);
        return complaintRepository
                .findByUsernameOrderByCreatedAtDesc(username);
    }

    // ============================================
    // Update complaint status
    // ============================================
    public Complaint updateStatus(Long id, String status) {
        log.info("🔄 Updating status ID={} to {}", id, status);

        Complaint complaint = complaintRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Complaint not found: " + id));

        if (!status.equals("PENDING") &&
                !status.equals("IN_PROGRESS") &&
                !status.equals("RESOLVED")) {
            throw new RuntimeException("Invalid status!");
        }

        complaint.setStatus(status);
        return complaintRepository.save(complaint);
    }

    // ============================================
    // Re-classify complaint using ML
    // ============================================
    public Complaint reclassify(Long id) {
        log.info("🤖 Re-classifying ID={}", id);

        Complaint complaint = complaintRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Complaint not found: " + id));

        MlServiceClient.MlResponse prediction =
                mlServiceClient.predict(
                        complaint.getTitle(),
                        complaint.getDescription());

        complaint.setDepartment(prediction.getDepartment());
        complaint.setPriority(prediction.getPriority());
        complaint.setConfidence(prediction.getConfidence());

        return complaintRepository.save(complaint);
    }

    // ============================================
    // Delete complaint (Admin only!)
    // Only RESOLVED complaints!
    // ============================================
    public void deleteComplaint(Long id) {
        log.info("🗑️ Deleting complaint ID={}", id);

        Complaint complaint = complaintRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Complaint not found: " + id));

        if (!complaint.getStatus().equals("RESOLVED")) {
            throw new RuntimeException(
                    "Only RESOLVED complaints can be deleted! " +
                            "Current status: " + complaint.getStatus());
        }

        complaintRepository.deleteById(id);
        log.info("✅ Deleted! ID={}", id);
    }
}