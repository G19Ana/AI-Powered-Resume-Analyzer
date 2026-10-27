package com.resume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.AnalysisResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final RestClient client;
    private final String defaultKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private String cachedModel;

    public GeminiService(@Value("${gemini.api.key:}") String defaultKey) {
        this.defaultKey = defaultKey;
        this.client = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public AnalysisResult analyze(String resumeText, String jobDesc, String headerKey) throws Exception {
        String key = (headerKey != null && !headerKey.isBlank()) ? headerKey.trim() : defaultKey;
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Gemini API key is missing. Paste your free key in the top right input.");
        }

        String model = resolveModel(key);

        String prompt = """
            You are an expert HR auditor. Compare this resume against the job description.
            Return ONLY a valid JSON object with no markdown backticks matching this schema:
            {
              "matchScore": 85,
              "candidateName": "Candidate Name",
              "keywordGaps": ["missing skill 1", "missing skill 2"],
              "strengths": ["candidate strength 1", "candidate strength 2"],
              "improvements": ["concrete suggestion 1", "concrete suggestion 2"],
              "rewrittenBullets": [
                {
                  "original": "original text from resume",
                  "improved": "impactful metric-driven rewrite"
                }
              ]
            }

            Resume text:
            %s

            Job description:
            %s
            """.formatted(resumeText, jobDesc);

        Map<String, Object> body = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig", Map.of("responseMimeType", "application/json")
        );

        try {
            JsonNode res = client.post()
                    .uri("/" + model + ":generateContent?key=" + key)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (res == null) throw new RuntimeException("Empty response from Gemini.");

            String raw = res.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            String cleaned = raw.replaceAll("(?s)^```json\\s*", "").replaceAll("(?s)^```\\s*", "").replaceAll("(?s)```\\s*$", "").trim();
            return mapper.readValue(cleaned, AnalysisResult.class);
        } catch (RestClientResponseException ex) {
            cachedModel = null;
            throw new RuntimeException("Google API error: " + ex.getResponseBodyAsString());
        }
    }

    private String resolveModel(String key) {
        if (cachedModel != null) return cachedModel;

        try {
            JsonNode res = client.get()
                    .uri("/models?key=" + key)
                    .retrieve()
                    .body(JsonNode.class);

            if (res == null || !res.has("models")) {
                cachedModel = "models/gemini-1.5-flash-latest";
                return cachedModel;
            }

            List<String> available = new ArrayList<>();
            res.path("models").forEach(m -> {
                boolean canGenerate = false;
                for (JsonNode method : m.path("supportedGenerationMethods")) {
                    if ("generateContent".equals(method.asText())) {
                        canGenerate = true;
                        break;
                    }
                }
                if (canGenerate) available.add(m.path("name").asText());
            });

            for (String name : available) {
                if (name.contains("flash") && (name.contains("2.0") || name.contains("1.5") || name.contains("latest"))) {
                    cachedModel = name;
                    return cachedModel;
                }
            }

            for (String name : available) {
                if (name.contains("gemini")) {
                    cachedModel = name;
                    return cachedModel;
                }
            }

            if (!available.isEmpty()) {
                cachedModel = available.get(0);
                return cachedModel;
            }
        } catch (Exception ignored) {
        }

        cachedModel = "models/gemini-1.5-flash-latest";
        return cachedModel;
    }
}
