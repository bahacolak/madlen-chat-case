package com.madlen.chat.util;

import com.madlen.chat.model.Conversation;
import com.madlen.chat.model.Message;

/**
 * Utility class for creating Message entities
 */
public final class MessageFactory {
    
    private MessageFactory() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Creates a user message from chat request data
     * 
     * @param conversation The conversation this message belongs to
     * @param content The message content
     * @param model The model used for this conversation
     * @param imageBase64 Optional base64 encoded image (without prefix)
     * @return A new Message entity ready to be saved
     */
    public static Message createUserMessage(Conversation conversation, String content, String model, String imageBase64) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setRole(Message.MessageRole.USER);
        message.setContent(content);
        message.setModel(model);
        
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            message.setImageUrl(Constants.IMAGE_DATA_PREFIX + imageBase64);
        }
        
        return message;
    }
    
    /**
     * Creates an assistant message
     * 
     * @param conversation The conversation this message belongs to
     * @param content The assistant's response content
     * @param model The model used for this conversation
     * @return A new Message entity ready to be saved
     */
    public static Message createAssistantMessage(Conversation conversation, String content, String model) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setRole(Message.MessageRole.ASSISTANT);
        message.setContent(content);
        message.setModel(model);
        
        return message;
    }
}
