package com.smartcomplaints.complaint.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Component
@Slf4j
public class MlServiceClient {

    private final RestTemplate restTemplate;

    // Reads from application.properties
    // Uses localhost:8000 locally
    // Uses Railway URL in production
    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    public MlServiceClient(
            RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(90))
                .build();
    }

    public MlResponse predict(
            String title, String description) {
        try {
            String url = mlServiceUrl + "/predict";
            log.info("🤖 Calling ML: {}", url);

            MlRequest request =
                    new MlRequest(title, description);
            MlResponse response = restTemplate.postForObject(
                    url, request, MlResponse.class);

            if (response != null) {
                log.info("✅ ML Response: dept={}, priority={}",
                        response.getDepartment(),
                        response.getPriority());
                return response;
            }
        } catch (Exception e) {
            log.error("❌ ML Service error: {}",
                    e.getMessage());
        }

        // Default fallback
        MlResponse fallback = new MlResponse();
        fallback.setDepartment("UNASSIGNED");
        fallback.setPriority("MEDIUM");
        fallback.setConfidence("0%");
        return fallback;
    }

    // Inner classes
    public static class MlRequest {
        private String title;
        private String description;

        public MlRequest(String title,
                         String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() { return title; }
        public String getDescription() {
            return description; }
    }

    public static class MlResponse {
        private String department;
        private String priority;
        private String confidence;

        public String getDepartment() {
            return department; }
        public String getPriority() {
            return priority; }
        public String getConfidence() {
            return confidence; }

        public void setDepartment(String d) {
            this.department = d; }
        public void setPriority(String p) {
            this.priority = p; }
        public void setConfidence(String c) {
            this.confidence = c; }
    }
}