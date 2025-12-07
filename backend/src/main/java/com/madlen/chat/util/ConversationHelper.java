package com.madlen.chat.util;

import com.madlen.chat.dto.ChatRequest;
import com.madlen.chat.dto.ConversationDto;
import com.madlen.chat.exception.ResourceNotFoundException;
import com.madlen.chat.model.Conversation;
import com.madlen.chat.model.Message;
import com.madlen.chat.repository.ConversationRepository;
import com.madlen.chat.service.ConversationService;

import java.util.List;

public final class ConversationHelper {
    
    private ConversationHelper() {
    }
    
    public static Conversation getOrCreateConversation(
            ChatRequest request,
            Long userId,
            ConversationRepository conversationRepository,
            ConversationService conversationService) {
        
        if (request.getConversationId() != null) {
            return conversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation", request.getConversationId()));
        } else {
            ConversationDto newConv = conversationService.createConversation(userId);
            return conversationRepository.findById(newConv.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation", newConv.getId()));
        }
    }
    
    public static void updateConversationTitleIfNeeded(
            Conversation conversation,
            String titleSource,
            ConversationRepository conversationRepository) {
        
        List<Message> conversationMessages = conversation.getMessages();
        boolean isNewConversation = Constants.DEFAULT_CONVERSATION_TITLE.equals(conversation.getTitle());
        boolean hasFewMessages = conversationMessages == null || conversationMessages.size() <= 2;
        
        if (isNewConversation && hasFewMessages && titleSource != null && !titleSource.isEmpty()) {
            String title = titleSource.length() > Constants.MAX_TITLE_LENGTH
                    ? titleSource.substring(0, Constants.MAX_TITLE_LENGTH) + Constants.TITLE_ELLIPSIS
                    : titleSource;
            conversation.setTitle(title);
            conversationRepository.save(conversation);
        }
    }
}
