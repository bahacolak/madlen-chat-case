package com.madlen.chat.service;

import com.madlen.chat.dto.ConversationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConversationService {
    ConversationDto createConversation(Long userId);

    List<ConversationDto> getUserConversations(Long userId);

    Page<ConversationDto> getUserConversations(Long userId, Pageable pageable);

    ConversationDto getConversationById(Long conversationId, Long userId);

    void deleteConversation(Long conversationId, Long userId);

    Page<ConversationDto.MessageDto> getConversationMessages(Long conversationId, Long userId, Pageable pageable);
}
