package com.madlen.chat.service;

import java.util.List;
import java.util.Map;

public interface OpenRouterService {
    String sendChatMessage(String message, String model, List<Map<String, String>> messages, String image);
    List<Map<String, Object>> getAvailableModels();
}

