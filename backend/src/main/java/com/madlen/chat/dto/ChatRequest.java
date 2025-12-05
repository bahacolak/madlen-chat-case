package com.madlen.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Model is required")
    private String model;

    private Long conversationId; // Optional: if not provided, a new conversation will be created
    
    private String image; // Optional: base64 encoded image (data:image/jpeg;base64, prefix is optional)
}

