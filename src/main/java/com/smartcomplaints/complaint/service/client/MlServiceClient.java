package com.smartcomplaints.complaint.service.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

// ============================================
// MlServiceClient.java
// This class calls our Python FastAPI ML service
// It sends complaint text and gets back
// department + priority predictions
// ============================================

@Component
@Slf4j
public class MlServiceClient {

    // URL of our Python FastAPI service
    private static final String ML_SERVICE_URL =
            "http://localhost:8000/predict";

    private final RestTemplate restTemplate;

    public MlServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    // ============================================
    // Request class - what we send to ML service
    // ============================================
    @Data
    public static class MlRequest {
        private String title;
        private String description;

        public MlRequest(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    // ============================================
    // Response class - what ML service returns
    // ============================================
    @Data
    public static class MlResponse {
        private String department;
        private String priority;
        private String confidence;
    }

    // ============================================
    // Main method - calls the ML service
    // ============================================
    public MlResponse predict(String title, String description) {
        try {
            log.info("🤖 Calling ML service for: {}", title);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request body
            MlRequest request = new MlRequest(title, description);

            // Wrap request with headers
            HttpEntity<MlRequest> entity =
                    new HttpEntity<>(request, headers);

            // Call Python FastAPI service
            ResponseEntity<MlResponse> response = restTemplate.exchange(
                    ML_SERVICE_URL,
                    HttpMethod.POST,
                    entity,
                    MlResponse.class
            );

            MlResponse mlResponse = response.getBody();
            log.info("✅ ML Prediction: department={}, priority={}, confidence={}",
                    mlResponse.getDepartment(),
                    mlResponse.getPriority(),
                    mlResponse.getConfidence());

            return mlResponse;

        } catch (Exception e) {
            // If ML service is down, don't crash!
            // Just return defaults
            log.error("❌ ML service error: {}", e.getMessage());
            log.warn("⚠️ Using default values: UNASSIGNED, MEDIUM");

            MlResponse defaultResponse = new MlResponse();
            defaultResponse.setDepartment("UNASSIGNED");
            defaultResponse.setPriority("MEDIUM");
            defaultResponse.setConfidence("0%");
            return defaultResponse;
        }
    }
}