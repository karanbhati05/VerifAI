package com.verifai.backend.controller;

import com.verifai.backend.dto.AiResponse;
import com.verifai.backend.model.VerificationRequest;
import com.verifai.backend.repository.VerificationRepository;
import com.verifai.backend.service.KycService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KycController.class)
public class KycControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KycService kycService;

    @MockBean
    private VerificationRepository verificationRepository;

    @Test
    void testUploadKyc_ApprovedWhenFaceAndNameMatch() throws Exception {
        AiResponse mockAiResponse = new AiResponse();
        mockAiResponse.setVerified(true);
        mockAiResponse.setConfidence(0.92);
        mockAiResponse.setExtractedText("Government Identity Card\nName: Karan Bhati\nValid: True");

        when(kycService.verifyWithAi(any(), any())).thenReturn(mockAiResponse);
        when(verificationRepository.save(any(VerificationRequest.class))).thenAnswer(i -> i.getArguments()[0]);

        MockMultipartFile idCard = new MockMultipartFile("idCard", "id.jpg", "image/jpeg", "fake-id-content".getBytes());
        MockMultipartFile selfie = new MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", "fake-selfie-content".getBytes());

        mockMvc.perform(multipart("/api/kyc/upload")
                        .file(idCard)
                        .file(selfie)
                        .param("name", "Karan Bhati")
                        .param("email", "karan@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.nameMatch").value(true))
                .andExpect(jsonPath("$.confidenceScore").value(0.92));
    }

    @Test
    void testUploadKyc_RejectedWhenNameMismatch() throws Exception {
        AiResponse mockAiResponse = new AiResponse();
        mockAiResponse.setVerified(true);
        mockAiResponse.setConfidence(0.85);
        mockAiResponse.setExtractedText("Identity Card\nName: Different Person\nID: 999");

        when(kycService.verifyWithAi(any(), any())).thenReturn(mockAiResponse);
        when(verificationRepository.save(any(VerificationRequest.class))).thenAnswer(i -> i.getArguments()[0]);

        MockMultipartFile idCard = new MockMultipartFile("idCard", "id.jpg", "image/jpeg", "fake-id".getBytes());
        MockMultipartFile selfie = new MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", "fake-selfie".getBytes());

        mockMvc.perform(multipart("/api/kyc/upload")
                        .file(idCard)
                        .file(selfie)
                        .param("name", "Karan Bhati")
                        .param("email", "karan@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.nameMatch").value(false));
    }

    @Test
    void testUploadKyc_RejectedWhenFaceMismatch() throws Exception {
        AiResponse mockAiResponse = new AiResponse();
        mockAiResponse.setVerified(false);
        mockAiResponse.setConfidence(0.20);
        mockAiResponse.setExtractedText("Name: Karan Bhati");

        when(kycService.verifyWithAi(any(), any())).thenReturn(mockAiResponse);
        when(verificationRepository.save(any(VerificationRequest.class))).thenAnswer(i -> i.getArguments()[0]);

        MockMultipartFile idCard = new MockMultipartFile("idCard", "id.jpg", "image/jpeg", "fake-id".getBytes());
        MockMultipartFile selfie = new MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", "fake-selfie".getBytes());

        mockMvc.perform(multipart("/api/kyc/upload")
                        .file(idCard)
                        .file(selfie)
                        .param("name", "Karan Bhati")
                        .param("email", "karan@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"));
    }
}
