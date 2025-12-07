package com.madlen.chat.util;

import com.madlen.chat.model.Message;
import com.madlen.chat.repository.MessageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for building message history for API requests
 */
public final class MessageHistoryBuilder {
    
    private MessageHistoryBuilder() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Builds message history from conversation messages for API requests
     * 
     * @param conversationId The conversation ID
     * @param messageRepository Repository for fetching messages
     * @return List of message maps with "role" and "content" keys
     */
    public static List<Map<String, String>> buildMessageHistory(
            Long conversationId,
            MessageRepository messageRepository) {
        
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<Map<String, String>> history = new ArrayList<>();
        
        for (Message message : messages) {
            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("role", message.getRole().name().toLowerCase());
            msgMap.put("content", message.getContent());
            history.add(msgMap);
        }
        
        return history;
    }
}
