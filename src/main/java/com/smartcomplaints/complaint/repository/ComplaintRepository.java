package com.smartcomplaints.complaint.repository;

import com.smartcomplaints.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository
        extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUsername(String username);

    List<Complaint> findByUsernameOrderByCreatedAtDesc(
            String username);
}