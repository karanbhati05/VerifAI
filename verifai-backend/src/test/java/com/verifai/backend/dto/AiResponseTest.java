package com.verifai.backend.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testJsonDeserialization() throws Exception {
        String json = "{\"match\": true, \"confidence\": 0.95, \"extracted_text\": \"Sample OCR Text\"}";

        AiResponse response = objectMapper.readValue(json, AiResponse.class);

        assertTrue(response.isFaceMatch());
        assertEquals(0.95, response.getConfidenceScore(), 0.001);
        assertEquals("Sample OCR Text", response.getExtractedText());
    }

    @Test
    void testNegativeFaceMatch() throws Exception {
        String json = "{\"match\": false, \"confidence\": 0.15, \"extracted_text\": \"Different Name\"}";

        AiResponse response = objectMapper.readValue(json, AiResponse.class);

        assertFalse(response.isFaceMatch());
        assertEquals(0.15, response.getConfidenceScore(), 0.001);
    }
}
