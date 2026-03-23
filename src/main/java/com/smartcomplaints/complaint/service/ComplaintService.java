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

        long startTime = System.currentTimeMillis(); // For timing
        log.info("📝 [START] New complaint from {}: {}", username, complaint.getTitle());

        complaint.setUsername(username);

        long mlStartTime = System.currentTimeMillis();
        MlServiceClient.MlResponse prediction = null;

        try {
            prediction = mlServiceClient.predict(
                    complaint.getTitle(),
                    complaint.getDescription());

            long mlDuration = System.currentTimeMillis() - mlStartTime;
            log.info("🤖 [ML] Prediction: Dept={}, Priority={}, Confidence={} (took {}ms)",
                    prediction.getDepartment(),
                    prediction.getPriority(),
                    prediction.getConfidence(),
                    mlDuration);

        } catch (Exception e) {
            log.error("❌ [ML] Service failed: {}", e.getMessage());
            // Set default values if ML fails
            prediction = new MlServiceClient.MlResponse();
            prediction.setDepartment("UNASSIGNED");
            prediction.setPriority("MEDIUM");
            prediction.setConfidence("0%");
        }

        complaint.setDepartment(prediction.getDepartment());
        complaint.setPriority(prediction.getPriority());
        complaint.setConfidence(prediction.getConfidence());

        // Verification status based on photo
        if (complaint.getPhoto() != null
                && !complaint.getPhoto().isEmpty()) {
            if (complaint.getPhotoLatitude() != null
                    && complaint.getPhotoLongitude() != null) {
                complaint.setVerificationStatus("VERIFIED");
                log.info("📸 [PHOTO] With GPS — lat={}, lng={}",
                        complaint.getPhotoLatitude(),
                        complaint.getPhotoLongitude());
            } else {
                complaint.setVerificationStatus("UNVERIFIED");
                log.info("⚠️ [PHOTO] No GPS data");
            }
        } else {
            complaint.setVerificationStatus("NO_PHOTO");
            log.info("❌ [PHOTO] No photo uploaded");
        }

        long dbStartTime = System.currentTimeMillis();
        Complaint saved = complaintRepository.save(complaint);
        long dbDuration = System.currentTimeMillis() - dbStartTime;

        long totalDuration = System.currentTimeMillis() - startTime;

        log.info("✅ [COMPLETE] Complaint #{} saved | Dept={} | Priority={} | Verification={} | DB:{}ms | Total:{}ms",
                saved.getId(),
                saved.getDepartment(),
                saved.getPriority(),
                saved.getVerificationStatus(),
                dbDuration,
                totalDuration);

        return saved;
    }

    public List<Complaint> getAllComplaints() {
        long startTime = System.currentTimeMillis();
        log.info("📋 [FETCH] Getting all complaints");

        List<Complaint> complaints = complaintRepository.findAll();

        log.info("✅ [FETCH] Retrieved {} complaints (took {}ms)",
                complaints.size(),
                System.currentTimeMillis() - startTime);

        return complaints;
    }

    public List<Complaint> getMyComplaints(String username) {
        long startTime = System.currentTimeMillis();
        log.info("📋 [FETCH] Getting complaints for user: {}", username);

        List<Complaint> complaints = complaintRepository
                .findByUsernameOrderByCreatedAtDesc(username);

        log.info("✅ [FETCH] Retrieved {} complaints for {} (took {}ms)",
                complaints.size(),
                username,
                System.currentTimeMillis() - startTime);

        return complaints;
    }

    public Complaint updateStatus(Long id, String status) {
        long startTime = System.currentTimeMillis();
        log.info("🔄 [UPDATE] Changing status of complaint #{} to: {}", id, status);

        Complaint complaint = complaintRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("❌ [UPDATE] Complaint #{} not found", id);
                    return new RuntimeException("Complaint not found: " + id);
                });

        if (!status.equals("PENDING") &&
                !status.equals("IN_PROGRESS") &&
                !status.equals("RESOLVED")) {
            log.error("❌ [UPDATE] Invalid status: {}", status);
            throw new RuntimeException("Invalid status!");
        }

        String oldStatus = complaint.getStatus();
        complaint.setStatus(status);
        Complaint updated = complaintRepository.save(complaint);

        log.info("✅ [UPDATE] Complaint #{} status changed from {} to {} (took {}ms)",
                id, oldStatus, status, System.currentTimeMillis() - startTime);

        return updated;
    }

    public Complaint reclassify(Long id) {
        long startTime = System.currentTimeMillis();
        log.info("🤖 [RECLASSIFY] Re-classifying complaint #{}", id);

        Complaint complaint = complaintRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("❌ [RECLASSIFY] Complaint #{} not found", id);
                    return new RuntimeException("Complaint not found: " + id);
                });

        String oldDept = complaint.getDepartment();
        String oldPriority = complaint.getPriority();

        long mlStartTime = System.currentTimeMillis();
        MlServiceClient.MlResponse prediction = null;

        try {
            prediction = mlServiceClient.predict(
                    complaint.getTitle(),
                    complaint.getDescription());

            log.info("🤖 [RECLASSIFY] ML result: {} (from {}) | Confidence: {} (took {}ms)",
                    prediction.getDepartment(),
                    oldDept,
                    prediction.getConfidence(),
                    System.currentTimeMillis() - mlStartTime);

        } catch (Exception e) {
            log.error("❌ [RECLASSIFY] ML service failed: {}", e.getMessage());
            prediction = new MlServiceClient.MlResponse();
            prediction.setDepartment("UNASSIGNED");
            prediction.setPriority("MEDIUM");
            prediction.setConfidence("0%");
        }

        complaint.setDepartment(prediction.getDepartment());
        complaint.setPriority(prediction.getPriority());
        complaint.setConfidence(prediction.getConfidence());

        Complaint updated = complaintRepository.save(complaint);

        log.info("✅ [RECLASSIFY] Complaint #{} reclassified from {}/{} to {}/{} (took {}ms)",
                id,
                oldDept, oldPriority,
                updated.getDepartment(),
                updated.getPriority(),
                System.currentTimeMillis() - startTime);

        return updated;
    }

    public void deleteComplaint(Long id) {
        log.info("🗑️ [DELETE] Attempting to delete complaint #{}", id);

        Complaint complaint = complaintRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("❌ [DELETE] Complaint #{} not found", id);
                    return new RuntimeException("Complaint not found: " + id);
                });

        if (!complaint.getStatus().equals("RESOLVED")) {
            log.warn("⚠️ [DELETE] Cannot delete complaint #{}. Status: {} (only RESOLVED allowed)",
                    id, complaint.getStatus());
            throw new RuntimeException(
                    "Only RESOLVED complaints can be deleted! " +
                            "Current status: " + complaint.getStatus());
        }

        complaintRepository.deleteById(id);
        log.info("✅ [DELETE] Complaint #{} deleted successfully", id);
    }
}