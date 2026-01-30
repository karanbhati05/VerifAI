package com.verifai.backend.repository;

import com.verifai.backend.model.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationRequest, Long> {
    // This gives us the save() method automatically
}