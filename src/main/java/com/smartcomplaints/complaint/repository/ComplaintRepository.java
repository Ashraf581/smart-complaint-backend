package com.smartcomplaints.complaint.repository;

import com.smartcomplaints.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUsernameOrderByCreatedAtDesc(String username);
}