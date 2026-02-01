package com.verifai.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiResponse {

    // --- THE FIX IS HERE ---
    // We tell Java: "When you see 'match' in JSON, put it in this variable."
    @JsonProperty("match")
    private boolean verified;

    private double confidence;

    @JsonProperty("extracted_text")
    private String extractedText;

    // --- Getters and Setters ---
    public boolean isFaceMatch() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public double getConfidenceScore() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
}