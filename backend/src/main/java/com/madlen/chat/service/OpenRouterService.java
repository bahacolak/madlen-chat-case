package com.madlen.chat.service;

import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;

public interface OpenRouterService {
    String sendChatMessage(String message, String model, List<Map<String, String>> messages, String image);

    Flux<String> streamChatMessage(String message, String model, List<Map<String, String>> messages, String image);

    List<Map<String, Object>> getAvailableModels();
}
