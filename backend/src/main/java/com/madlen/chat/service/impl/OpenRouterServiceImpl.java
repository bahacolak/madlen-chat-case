package com.madlen.chat.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.madlen.chat.exception.OpenRouterException;
import com.madlen.chat.service.OpenRouterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenRouterServiceImpl implements OpenRouterService {

    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenRouterServiceImpl(@Value("${openrouter.api.key}") String apiKey,
            @Value("${openrouter.api.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader("HTTP-Referer", "http://localhost:8080")
                .defaultHeader("X-Title", "Chat Application")
                .build();
    }

    @Override
    public String sendChatMessage(String message, String model, List<Map<String, String>> messages, String image) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, Object>> requestMessages = new ArrayList<>();

        // Add conversation history
        if (messages != null) {
            for (Map<String, String> msg : messages) {
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("role", msg.get("role"));
                msgMap.put("content", msg.get("content"));
                requestMessages.add(msgMap);
            }
        }

        // Add current message
        Map<String, Object> currentMessage = new HashMap<>();
        currentMessage.put("role", "user");

        if (image != null && !image.isEmpty()) {
            List<Map<String, Object>> contentList = new ArrayList<>();
            contentList.add(Map.of("type", "text", "text", message));
            contentList.add(Map.of("type", "image_url", "image_url", Map.of("url", "data:image/jpeg;base64," + image)));
            currentMessage.put("content", contentList);
        } else {
            currentMessage.put("content", message);
        }

        requestMessages.add(currentMessage);
        requestBody.put("messages", requestMessages);

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                    return (String) messageObj.get("content");
                }
            }

            throw new OpenRouterException("Failed to get response from OpenRouter: Invalid response format");
        } catch (WebClientResponseException e) {
            String errorMessage = "OpenRouter API error: " + e.getStatusCode() + " " + e.getStatusText();
            if (e.getResponseBodyAsString() != null && !e.getResponseBodyAsString().isEmpty()) {
                try {
                    // Try to parse error response for better error message
                    Map<String, Object> errorBody = e.getResponseBodyAs(Map.class);
                    if (errorBody != null && errorBody.containsKey("error")) {
                        Object errorObj = errorBody.get("error");
                        if (errorObj instanceof Map) {
                            Map<String, Object> errorMap = (Map<String, Object>) errorObj;
                            if (errorMap.containsKey("message")) {
                                errorMessage += " - " + errorMap.get("message");
                            }
                        }
                    }
                } catch (Exception parseEx) {
                    // If parsing fails, use default message
                }
            }
            throw new OpenRouterException(errorMessage + " from POST " + e.getRequest().getURI(), e);
        } catch (Exception e) {
            throw new OpenRouterException("Failed to communicate with OpenRouter API: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> streamChatMessage(String message, String model, List<Map<String, String>> messages,
            String image) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("stream", true);

        List<Map<String, Object>> requestMessages = new ArrayList<>();

        // Add conversation history
        if (messages != null) {
            for (Map<String, String> msg : messages) {
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("role", msg.get("role"));
                msgMap.put("content", msg.get("content"));
                requestMessages.add(msgMap);
            }
        }

        // Add current message
        Map<String, Object> currentMessage = new HashMap<>();
        currentMessage.put("role", "user");

        if (image != null && !image.isEmpty()) {
            List<Map<String, Object>> contentList = new ArrayList<>();
            contentList.add(Map.of("type", "text", "text", message));
            contentList.add(Map.of("type", "image_url", "image_url", Map.of("url", "data:image/jpeg;base64," + image)));
            currentMessage.put("content", contentList);
        } else {
            currentMessage.put("content", message);
        }

        requestMessages.add(currentMessage);
        requestBody.put("messages", requestMessages);

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isEmpty() && !line.equals("[DONE]") && !line.contains("[DONE]"))
                .map(line -> {
                    try {
                        // Handle both "data: {json}" and raw "{json}" formats
                        String jsonStr = line.startsWith("data: ") ? line.substring(6) : line;
                        if (jsonStr.isEmpty() || jsonStr.equals("[DONE]")) {
                            return "";
                        }
                        JsonNode root = objectMapper.readTree(jsonStr);
                        JsonNode choices = root.get("choices");
                        if (choices != null && choices.isArray() && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null && delta.has("content")) {
                                return delta.get("content").asText();
                            }
                        }
                        return "";
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(content -> !content.isEmpty())
                .onErrorResume(e -> {
                    return Flux.error(new OpenRouterException("Streaming failed: " + e.getMessage(), e));
                });
    }

    @Override
    public List<Map<String, Object>> getAvailableModels() {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("/models")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> allModels = (List<Map<String, Object>>) response.get("data");
                List<Map<String, Object>> processedModels = new ArrayList<>();
                for (Map<String, Object> model : allModels) {
                    Map<String, Object> processedModel = new HashMap<>(model);
                    boolean isFree = false;
                    if (model.containsKey("pricing")) {
                        Object pricingObj = model.get("pricing");
                        if (pricingObj instanceof Map) {
                            Map<String, Object> pricing = (Map<String, Object>) pricingObj;
                            Object promptPrice = pricing.get("prompt");
                            Object completionPrice = pricing.get("completion");
                            isFree = (promptPrice != null && String.valueOf(promptPrice).equals("0")) &&
                                    (completionPrice != null && String.valueOf(completionPrice).equals("0"));
                        }
                    }
                    processedModel.put("free", isFree);

                    boolean supportsVision = false;
                    if (model.containsKey("architecture")) {
                        Object archObj = model.get("architecture");
                        if (archObj instanceof Map) {
                            Map<String, Object> architecture = (Map<String, Object>) archObj;
                            Object inputModalities = architecture.get("input_modalities");
                            if (inputModalities instanceof List) {
                                List<String> modalities = (List<String>) inputModalities;
                                supportsVision = modalities.contains("image");
                            }
                        }
                    }
                    processedModel.put("supportsVision", supportsVision);

                    processedModels.add(processedModel);
                }
                return processedModels;
            }
        } catch (Exception e) {
            // Fallback to hardcoded free models if API call fails
            System.err.println("Failed to fetch models from OpenRouter: " + e.getMessage());
        }

        // Return verified free models that work (including vision models)
        List<Map<String, Object>> freeModels = new ArrayList<>();
        freeModels.add(Map.of("id", "meta-llama/llama-3.2-3b-instruct:free", "name", "Meta Llama 3.2 3B (Free)", "free",
                true, "supportsVision", false));
        freeModels.add(Map.of("id", "amazon/nova-2-lite-v1:free", "name", "Amazon Nova 2 Lite (Free)", "free", true,
                "supportsVision", true));
        freeModels.add(Map.of("id", "google/gemma-3-4b-it:free", "name", "Google Gemma 3 4B (ImageFree)", "free", true,
                "supportsVision", false));
        freeModels.add(Map.of("id", "openai/gpt-oss-20b:free", "name", "OpenAI GPT-OSS 20B (Free)", "free", true,
                "supportsVision", false));
        return freeModels;
    }
}
