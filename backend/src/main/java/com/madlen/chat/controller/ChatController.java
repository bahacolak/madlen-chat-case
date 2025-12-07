package com.madlen.chat.controller;

import com.madlen.chat.dto.ChatRequest;
import com.madlen.chat.dto.ChatResponse;
import com.madlen.chat.dto.ModelInfo;
import com.madlen.chat.service.ChatService;
import com.madlen.chat.service.OpenRouterService;
import com.madlen.chat.util.AuthenticationHelper;
import com.madlen.chat.util.CacheConstants;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatController {
    
    private final ChatService chatService;
    private final OpenRouterService openRouterService;
    private final com.madlen.chat.service.UserService userService;
    
    public ChatController(ChatService chatService, 
                         OpenRouterService openRouterService,
                         com.madlen.chat.service.UserService userService) {
        this.chatService = chatService;
        this.openRouterService = openRouterService;
        this.userService = userService;
    }
    
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request,
                                                   Authentication authentication) {
        Long userId = AuthenticationHelper.getUserIdFromAuthentication(authentication, userService);
        ChatResponse response = chatService.sendMessage(request, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/models")
    public ResponseEntity<List<ModelInfo>> getModels() {
        List<Map<String, Object>> models = openRouterService.getAvailableModels();
        List<ModelInfo> modelInfos = models.stream()
                .map(model -> {
                    String id = (String) model.get("id");
                    String name = (String) model.getOrDefault("name", id);
                    Boolean free = (Boolean) model.getOrDefault("free", false);
                    return new ModelInfo(id, name, free);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(modelInfos);
    }
    
    @PostMapping("/models/refresh")
    @CacheEvict(value = CacheConstants.CACHE_MODELS, allEntries = true)
    public ResponseEntity<String> refreshModelsCache() {
        return ResponseEntity.ok("Models cache refreshed successfully");
    }
}

