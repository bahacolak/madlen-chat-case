package com.madlen.chat.service;

import com.madlen.chat.dto.ConversationDto;

import java.util.List;

public interface ConversationService {
    ConversationDto createConversation(Long userId);
    List<ConversationDto> getUserConversations(Long userId);
    ConversationDto getConversationById(Long conversationId, Long userId);
    void deleteConversation(Long conversationId, Long userId);
}

