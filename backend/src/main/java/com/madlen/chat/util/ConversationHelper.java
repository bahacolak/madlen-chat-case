package com.madlen.chat.util;

import com.madlen.chat.dto.ChatRequest;
import com.madlen.chat.dto.ConversationDto;
import com.madlen.chat.exception.ResourceNotFoundException;
import com.madlen.chat.model.Conversation;
import com.madlen.chat.model.Message;
import com.madlen.chat.repository.ConversationRepository;
import com.madlen.chat.service.ConversationService;

import java.util.List;

/**
 * Utility class for conversation-related operations
 */
public final class ConversationHelper {
    
    private ConversationHelper() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Gets an existing conversation or creates a new one
     * 
     * @param request The chat request containing optional conversationId
     * @param userId The user ID
     * @param conversationRepository Repository for conversation operations
     * @param conversationService Service for creating new conversations
     * @return The conversation (existing or newly created)
     * @throws ResourceNotFoundException if conversationId is provided but not found
     */
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
    
    /**
     * Updates conversation title if it's still the default title and conversation is new
     * 
     * @param conversation The conversation to update
     * @param titleSource The source text to use for the title (usually the first user message)
     * @param conversationRepository Repository for saving the conversation
     */
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
