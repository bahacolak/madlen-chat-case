package com.madlen.chat.service;

import com.madlen.chat.dto.ChatRequest;
import com.madlen.chat.dto.ChatResponse;

public interface ChatService {
    ChatResponse sendMessage(ChatRequest request, Long userId);
}

