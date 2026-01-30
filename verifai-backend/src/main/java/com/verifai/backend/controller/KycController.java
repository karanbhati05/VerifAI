package com.verifai.backend.controller;

import com.verifai.backend.model.VerificationRequest; // Ensure this import
import com.verifai.backend.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/kyc")
@CrossOrigin(origins = "*") // Allows your HTML to talk to Java
public class KycController {

    @Autowired
    private KycService kycService;

    @PostMapping("/upload")
    public ResponseEntity<VerificationRequest> uploadKyc(
            @RequestParam("idCard") MultipartFile idCard,
            @RequestParam("selfie") MultipartFile selfie) {

        try {
            // This returns the FULL object (with ID, Status, Score)
            VerificationRequest result = kycService.processKycSubmission(idCard, selfie);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}