package com.verifai.backend.model;

import jakarta.persistence.*;

@Entity
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Standardized field names to match Controller
    private String name;
    private String email;
    private String status;
    private double confidenceScore;

    // We can keep these if you want to store file paths too
    private String idCardPath;
    private String selfiePath;

    public VerificationRequest() {}

    // --- GETTERS AND SETTERS (These fix the "Cannot resolve method" errors) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; } // <--- FIXES THE ERROR

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; } // <--- FIXES THE ERROR

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    // Optional: Getters/Setters for paths if you use them
    public String getIdCardPath() { return idCardPath; }
    public void setIdCardPath(String idCardPath) { this.idCardPath = idCardPath; }

    public String getSelfiePath() { return selfiePath; }
    public void setSelfiePath(String selfiePath) { this.selfiePath = selfiePath; }
}