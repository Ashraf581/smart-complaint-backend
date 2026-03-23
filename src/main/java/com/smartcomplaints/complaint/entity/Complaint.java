package com.smartcomplaints.complaint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(name = "department")
    private String department;

    @Column(name = "status")
    private String status;

    @Column(name = "priority")
    private String priority;

    @Column(name = "confidence")
    private String confidence;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    // ── NEW FIELDS ──────────────────
    @Column(name = "photo", columnDefinition = "TEXT")
    private String photo;

    @Column(name = "photo_latitude")
    private Double photoLatitude;

    @Column(name = "photo_longitude")
    private Double photoLongitude;

    @Column(name = "verification_status")
    private String verificationStatus;
    // ─────────────────────────────────

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";

        if (this.department == null || this.department.isEmpty()) {
            this.department = "UNASSIGNED";
        }
        if (this.priority == null || this.priority.isEmpty()) {
            this.priority = "MEDIUM";
        }
        if (this.confidence == null || this.confidence.isEmpty()) {
            this.confidence = "0%";
        }
        // NEW — default verification status
        if (this.verificationStatus == null) {
            this.verificationStatus = "UNVERIFIED";
        }
    }
}