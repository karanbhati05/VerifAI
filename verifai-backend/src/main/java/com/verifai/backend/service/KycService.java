package com.verifai.backend.service;

import com.verifai.backend.dto.AiResponse; // Import the new DTO
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class KycService {

    private final String UPLOAD_DIR = "uploads/";
    private final RestTemplate restTemplate = new RestTemplateBuilder().build();

    // Renamed method to match Controller
    // Changed return type from VerificationRequest -> AiResponse
    public AiResponse verifyWithAi(MultipartFile idCard, MultipartFile selfie) throws IOException {

        // 1. Create upload directory if not exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        // 2. Save Files Locally (Required to send them to Python)
        String idCardFileName = UUID.randomUUID() + "_" + idCard.getOriginalFilename();
        Path idCardPath = Paths.get(UPLOAD_DIR + idCardFileName);
        Files.write(idCardPath, idCard.getBytes());

        String selfieFileName = UUID.randomUUID() + "_" + selfie.getOriginalFilename();
        Path selfiePath = Paths.get(UPLOAD_DIR + selfieFileName);
        Files.write(selfiePath, selfie.getBytes());

        // 3. CALL PYTHON AI ENGINE
        String pythonUrl = "http://127.0.0.1:5000/verify";

        try {
            // Prepare the files for sending
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("id_card", new FileSystemResource(idCardPath.toFile()));
            body.add("selfie", new FileSystemResource(selfiePath.toFile()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Send Request and automatically map JSON -> AiResponse object
            ResponseEntity<AiResponse> response = restTemplate.postForEntity(pythonUrl, requestEntity, AiResponse.class);

            return response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
            // In case of error, return a blank failed response so the app doesn't crash
            AiResponse failedResponse = new AiResponse();
            failedResponse.setVerified(false);
            failedResponse.setConfidence(0.0);
            failedResponse.setExtractedText("AI Connection Failed: " + e.getMessage());
            return failedResponse;
        }
    }
}