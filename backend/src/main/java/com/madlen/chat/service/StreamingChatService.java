package com.madlen.chat.service;

import com.madlen.chat.dto.ChatRequest;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface StreamingChatService {
    Flux<ServerSentEvent<String>> streamChat(ChatRequest request, Long userId);
}
