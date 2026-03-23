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

    public Complaint submitComplaint(
            Complaint complaint, String username) {

        log.info("📝 New complaint from {}: {}",
                username, complaint.getTitle());

        complaint.setUsername(username);

        MlServiceClient.MlResponse prediction =
                mlServiceClient.predict(
                        complaint.getTitle(),
                        complaint.getDescription());

        complaint.setDepartment(prediction.getDepartment());
        complaint.setPriority(prediction.getPriority());
        complaint.setConfidence(prediction.getConfidence());

        // ── NEW — set verification status ──────────────
        if (complaint.getPhoto() != null
                && !complaint.getPhoto().isEmpty()) {
            if (complaint.getPhotoLatitude() != null
                    && complaint.getPhotoLongitude() != null) {
                complaint.setVerificationStatus("VERIFIED");
                log.info("✅ Photo with GPS — VERIFIED");
            } else {
                complaint.setVerificationStatus("UNVERIFIED");
                log.info("⚠️ Photo without GPS — UNVERIFIED");
            }
        } else {
            complaint.setVerificationStatus("NO_PHOTO");
            log.info("❌ No photo — NO_PHOTO");
        }
        // ───────────────────────────────────────────────

        Complaint saved = complaintRepository.save(complaint);

        log.info("✅ Saved! ID={}, Dept={}, Priority={}, Verification={}",
                saved.getId(),
                saved.getDepartment(),
                saved.getPriority(),
                saved.getVerificationStatus());

        return saved;
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public List<Complaint> getMyComplaints(String username) {
        log.info("📋 Getting complaints for: {}", username);
        return complaintRepository
                .findByUsernameOrderByCreatedAtDesc(username);
    }

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