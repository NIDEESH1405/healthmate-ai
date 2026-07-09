package com.healthmate.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthmate.ai.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central service for all calls to the Groq LLM API (OpenAI-compatible /chat/completions).
 * Provides three access patterns used across the five agents:
 *   - chat(messages): full multi-turn conversation, plain text reply
 *   - singlePrompt(system, user): one-shot free-text completion
 *   - jsonPrompt(system, user): one-shot completion constrained to JSON output, safely parsed
 *
 * All calls fail soft: on any error (missing key, network failure, malformed response)
 * a descriptive fallback string / JSON node is returned instead of throwing to the caller,
 * so agent UIs can always render something meaningful.
 */
@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    public GroqService(WebClient groqWebClient, ObjectMapper objectMapper) {
        this.webClient = groqWebClient;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Multi-turn chat completion returning the assistant's plain-text reply. */
    public String chat(List<ChatMessage> messages) {
        return chat(messages, false);
    }

    /** One-shot completion given a system instruction and a single user prompt. */
    public String singlePrompt(String systemPrompt, String userPrompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));
        messages.add(ChatMessage.user(userPrompt));
        return chat(messages, false);
    }

    /**
     * One-shot completion where the model is instructed to answer ONLY with JSON.
     * Returns a parsed JsonNode; on failure returns an object node with an "error" field
     * so callers can detect and handle fallback behaviour gracefully.
     */
    public JsonNode jsonPrompt(String systemPrompt, String userPrompt) {
        List<ChatMessage> messages = new ArrayList<>();
        String jsonSystem = systemPrompt + "\n\nRespond ONLY with valid JSON. Do not include any prose, "
                + "explanation, or markdown code fences before or after the JSON object.";
        messages.add(ChatMessage.system(jsonSystem));
        messages.add(ChatMessage.user(userPrompt));
        String raw = chat(messages, true);
        return parseJsonSafely(raw);
    }

    private String chat(List<ChatMessage> messages, boolean jsonMode) {
        if (!isConfigured()) {
            log.warn("GROQ_API_KEY is not set; returning fallback response.");
            return jsonMode ? "{\"error\":\"LLM not configured\"}" : fallbackMessage();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.65);
        body.put("max_tokens", 1200);
        if (jsonMode) {
            Map<String, String> responseFormat = new LinkedHashMap<>();
            responseFormat.put("type", "json_object");
            body.put("response_format", responseFormat);
        }

        try {
            JsonNode response = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                            .filter(this::isRetryable))
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response == null) {
                return jsonMode ? "{\"error\":\"empty response\"}" : fallbackMessage();
            }

            JsonNode choices = response.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();
                return content == null || content.isBlank() ? fallbackMessage() : content;
            }
            return jsonMode ? "{\"error\":\"no choices returned\"}" : fallbackMessage();
        } catch (WebClientResponseException e) {
            log.error("Groq API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return jsonMode ? "{\"error\":\"Groq API error\"}" : fallbackMessage();
        } catch (Exception e) {
            log.error("Unexpected error calling Groq API", e);
            return jsonMode ? "{\"error\":\"unexpected error\"}" : fallbackMessage();
        }
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientResponseException wcre) {
            int status = wcre.getStatusCode().value();
            return status == 429 || status >= 500;
        }
        return false;
    }

    private JsonNode parseJsonSafely(String raw) {
        if (raw == null) {
            return objectMapper.createObjectNode().put("error", "no content");
        }
        String cleaned = raw.trim();
        // Strip markdown code fences if the model added them despite instructions.
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(json)?", "").trim();
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
            }
        }
        try {
            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            log.warn("Failed to parse Groq JSON response: {}", e.getMessage());
            return objectMapper.createObjectNode().put("error", "parse failure").put("raw", cleaned);
        }
    }

    private String fallbackMessage() {
        return "I'm unable to reach the AI service right now. Please check that GROQ_API_KEY is configured "
                + "and try again shortly. This is an informational assistant only and does not replace "
                + "professional medical advice.";
    }
}
