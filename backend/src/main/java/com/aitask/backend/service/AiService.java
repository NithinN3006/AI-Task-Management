package com.aitask.backend.service;

import com.aitask.backend.dto.AiGenerateResponse;
import com.aitask.backend.model.Priority;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public AiGenerateResponse generateTaskDetails(String title) {
        if (apiKey == null || apiKey.trim().isEmpty() || "your_gemini_api_key_here".equals(apiKey)) {
            logger.warn("GEMINI_API_KEY is not configured or empty. Running in mock mode.");
            return fallbackResponse();
        }

        try {
            String requestBody = buildGeminiRequest(title);
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            java.net.http.HttpResponse<String> response = java.net.http.HttpClient.newHttpClient()
                    .send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 400) {
                 logger.error("Gemini API Error: {} - {}", response.statusCode(), response.body());
                 return fallbackResponse();
            }

            return parseGeminiResponse(response.body());

        } catch (Exception e) {
            logger.error("Failed to generate task details from Gemini API: {}", e.getMessage());
            return fallbackResponse();
        }
    }

    private String buildGeminiRequest(String title) throws JsonProcessingException {
        // We use JSON structured output format for Gemini
        // generationConfig.responseMimeType = "application/json"
        
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "OBJECT");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> descProp = new HashMap<>();
        descProp.put("type", "STRING");
        descProp.put("description", "A detailed, actionable description of the task based on its title.");
        properties.put("description", descProp);
        
        Map<String, Object> priorityProp = new HashMap<>();
        priorityProp.put("type", "STRING");
        priorityProp.put("enum", List.of("LOW", "MEDIUM", "HIGH"));
        properties.put("priority", priorityProp);
        
        Map<String, Object> estHoursProp = new HashMap<>();
        estHoursProp.put("type", "INTEGER");
        properties.put("estimatedHours", estHoursProp);
        
        schema.put("properties", properties);
        schema.put("required", List.of("description", "priority", "estimatedHours"));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("responseSchema", schema);
        
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", "Generate a task description, priority, and estimated hours for this task title: \"" + title + "\"");
        
        Map<String, Object> contents = new HashMap<>();
        contents.put("parts", List.of(parts));
        
        Map<String, Object> root = new HashMap<>();
        root.put("contents", List.of(contents));
        root.put("generationConfig", generationConfig);

        return objectMapper.writeValueAsString(root);
    }

    private AiGenerateResponse parseGeminiResponse(String responseStr) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseStr);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String jsonText = parts.get(0).path("text").asText();
                    // parse the inner JSON string
                    JsonNode contentNode = objectMapper.readTree(jsonText);
                    
                    return AiGenerateResponse.builder()
                            .description(contentNode.path("description").asText(""))
                            .priority(Priority.valueOf(contentNode.path("priority").asText("MEDIUM")))
                            .estimatedHours(contentNode.path("estimatedHours").asInt(0))
                            .aiGenerated(true)
                            .build();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse Gemini API response: {}", e.getMessage());
        }
        return fallbackResponse();
    }

    private AiGenerateResponse fallbackResponse() {
        return AiGenerateResponse.builder()
                .description("")
                .priority(Priority.MEDIUM)
                .estimatedHours(null) // Request allows null for fallback
                .aiGenerated(false)
                .build();
    }
}
