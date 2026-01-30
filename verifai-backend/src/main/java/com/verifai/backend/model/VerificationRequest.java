package com.verifai.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_requests")
@Data // Lombok automatically generates Getters/Setters
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;
    private String email;

    private String idCardImagePath;
    private String selfieImagePath;

    private String verificationStatus; // PENDING, APPROVED, REJECTED
    private Double confidenceScore;

    // --- NEW FIELD FOR OCR TEXT ---
    // We use "TEXT" type because ID card data can be longer than 255 characters
    @Column(columnDefinition = "TEXT")
    private String extractedText;
    // ------------------------------

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.verificationStatus == null) {
            this.verificationStatus = "PENDING";
        }
    }
}