package com.verifai.backend.service;

// --- 1. CORE IMPORTS ---
import com.verifai.backend.model.VerificationRequest;
import com.verifai.backend.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// --- 2. FILE HANDLING IMPORTS ---
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// --- 3. REST TEMPLATE (WEB CLIENT) IMPORTS ---
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

// --- 4. JSON HANDLING (JACKSON) IMPORTS ---
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KycService {

    @Autowired
    private VerificationRepository verificationRepository;

    private final String UPLOAD_DIR = "uploads/";
    private final RestTemplate restTemplate = new RestTemplateBuilder().build();

    public VerificationRequest processKycSubmission(MultipartFile idCard, MultipartFile selfie) throws IOException {
        // 1. Create upload directory
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        // 2. Save Files Locally
        String idCardFileName = UUID.randomUUID() + "_" + idCard.getOriginalFilename();
        Path idCardPath = Paths.get(UPLOAD_DIR + idCardFileName);
        Files.write(idCardPath, idCard.getBytes());

        String selfieFileName = UUID.randomUUID() + "_" + selfie.getOriginalFilename();
        Path selfiePath = Paths.get(UPLOAD_DIR + selfieFileName);
        Files.write(selfiePath, selfie.getBytes());

        // 3. CALL PYTHON AI ENGINE
        String pythonUrl = "http://127.0.0.1:5000/verify";

        // Default values in case AI fails
        String verificationStatus = "MANUAL_REVIEW";
        Double confidence = 0.0;
        String extractedText = "No text extracted"; // <--- NEW VARIABLE

        try {
            // Prepare the files for sending
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("id_card", new FileSystemResource(idCardPath.toFile()));
            body.add("selfie", new FileSystemResource(selfiePath.toFile()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Send Request
            ResponseEntity<String> response = restTemplate.postForEntity(pythonUrl, requestEntity, String.class);

            // Parse Response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            boolean match = root.path("match").asBoolean();
            confidence = root.path("confidence").asDouble();

            // --- NEW: GRAB THE TEXT FROM PYTHON ---
            if (root.has("extracted_text")) {
                extractedText = root.path("extracted_text").asText();
            }
            // --------------------------------------

            verificationStatus = match ? "APPROVED" : "REJECTED";

        } catch (Exception e) {
            System.out.println("AI Engine Connection Failed: " + e.getMessage());
        }

        // 4. Save to Database
        VerificationRequest request = new VerificationRequest();
        request.setUserName("Demo User");
        request.setEmail("demo@example.com");
        request.setIdCardImagePath(idCardPath.toString());
        request.setSelfieImagePath(selfiePath.toString());

        request.setVerificationStatus(verificationStatus);
        request.setConfidenceScore(confidence);

        // --- NEW: SAVE TEXT TO DATABASE ---
        request.setExtractedText(extractedText);
        // ----------------------------------

        return verificationRepository.save(request);
    }
}