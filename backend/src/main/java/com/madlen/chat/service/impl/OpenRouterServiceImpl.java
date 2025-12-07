package com.madlen.chat.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.madlen.chat.exception.OpenRouterException;
import com.madlen.chat.service.OpenRouterService;
import com.madlen.chat.util.CacheConstants;
import com.madlen.chat.util.OpenRouterConstants;
import com.madlen.chat.util.OpenRouterRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterServiceImpl implements OpenRouterService {

    private static final Logger logger = LoggerFactory.getLogger(OpenRouterServiceImpl.class);
    
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
                .defaultHeader(OpenRouterConstants.HEADER_HTTP_REFERER, OpenRouterConstants.DEFAULT_HTTP_REFERER)
                .defaultHeader(OpenRouterConstants.HEADER_X_TITLE, OpenRouterConstants.DEFAULT_X_TITLE)
                .build();
    }

    @Override
    public String sendChatMessage(String message, String model, List<Map<String, String>> messages, String image) {
        Map<String, Object> requestBody = OpenRouterRequestBuilder.buildChatRequest(
                message, model, messages, image, false);

        try {
            Map<String, Object> response = webClient.post()
                    .uri(OpenRouterConstants.CHAT_COMPLETIONS_ENDPOINT)
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
                    logger.warn("Failed to parse error response body: {}", parseEx.getMessage());
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
        Map<String, Object> requestBody = OpenRouterRequestBuilder.buildChatRequest(
                message, model, messages, image, true);

        return webClient.post()
                .uri(OpenRouterConstants.CHAT_COMPLETIONS_ENDPOINT)
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isEmpty() 
                        && !line.equals(OpenRouterConstants.STREAM_DONE_MARKER) 
                        && !line.contains(OpenRouterConstants.STREAM_DONE_MARKER))
                .map(line -> {
                    try {
                        String jsonStr = line.startsWith(OpenRouterConstants.SSE_DATA_PREFIX) 
                                ? line.substring(OpenRouterConstants.SSE_DATA_PREFIX.length()) 
                                : line;
                        if (jsonStr.isEmpty() || jsonStr.equals(OpenRouterConstants.STREAM_DONE_MARKER)) {
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
                        logger.debug("Failed to parse streaming chunk: {}", e.getMessage());
                        return "";
                    }
                })
                .filter(content -> !content.isEmpty())
                .onErrorResume(e -> {
                    return Flux.error(new OpenRouterException("Streaming failed: " + e.getMessage(), e));
                });
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_MODELS)
    public List<Map<String, Object>> getAvailableModels() {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(OpenRouterConstants.MODELS_ENDPOINT)
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
                                supportsVision = modalities.contains(OpenRouterConstants.CONTENT_TYPE_IMAGE);
                            }
                        }
                    }
                    processedModel.put("supportsVision", supportsVision);

                    processedModels.add(processedModel);
                }
                return processedModels;
            }
        } catch (Exception e) {
            logger.error("Failed to fetch models from OpenRouter: {}", e.getMessage(), e);
        }

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
