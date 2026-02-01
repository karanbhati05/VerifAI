package com.verifai.backend.controller;

import com.verifai.backend.dto.AiResponse;
import com.verifai.backend.model.VerificationRequest;
import com.verifai.backend.repository.VerificationRepository;
import com.verifai.backend.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/kyc")
@CrossOrigin(origins = "*") // Allows frontend to talk to backend
public class KycController {

    // --- THESE WERE MISSING IN YOUR CODE ---
    @Autowired
    private KycService kycService;

    @Autowired
    private VerificationRepository verificationRepository;
    // ---------------------------------------

    @PostMapping("/upload")
    public ResponseEntity<?> uploadKyc(
            @RequestParam("idCard") MultipartFile idCard,
            @RequestParam("selfie") MultipartFile selfie,
            @RequestParam("name") String userName,
            @RequestParam("email") String userEmail
    ) {
        try {
            // 1. Call Python AI
            AiResponse aiResponse = kycService.verifyWithAi(idCard, selfie);

            // --- SMART MATCHING LOGIC START ---

            String extractedText = aiResponse.getExtractedText();
            if (extractedText == null) extractedText = "";

            // Normalize everything to lowercase
            String cleanOCR = extractedText.toLowerCase();
            String cleanInputName = userName.toLowerCase().trim();

            // Split the user's name into words (e.g., "Karan Bhati" -> ["karan", "bhati"])
            String[] nameParts = cleanInputName.split("\\s+");

            // Check if EACH word exists in the OCR text
            boolean allPartsFound = true;
            for (String part : nameParts) {
                if (!cleanOCR.contains(part)) {
                    allPartsFound = false;
                    break;
                }
            }

            boolean isNameMatch = allPartsFound;
            boolean isFaceMatch = aiResponse.isFaceMatch();

            // --- MATCHING LOGIC END ---

            String finalStatus = "REJECTED";
            if (isFaceMatch && isNameMatch) {
                finalStatus = "APPROVED";
            }

            // Save to Database
            VerificationRequest request = new VerificationRequest();
            request.setName(userName);
            request.setEmail(userEmail);
            request.setStatus(finalStatus);
            request.setConfidenceScore(aiResponse.getConfidenceScore());
            verificationRepository.save(request);

            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("verificationStatus", finalStatus);
            response.put("confidenceScore", aiResponse.getConfidenceScore());
            response.put("nameMatch", isNameMatch);
            response.put("extractedText", aiResponse.getExtractedText());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

}