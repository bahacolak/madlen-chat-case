package com.madlen.chat.util;

import com.madlen.chat.model.Conversation;
import com.madlen.chat.model.Message;

public final class MessageFactory {
    
    private MessageFactory() {
    }
    
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
    
    public static Message createAssistantMessage(Conversation conversation, String content, String model) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setRole(Message.MessageRole.ASSISTANT);
        message.setContent(content);
        message.setModel(model);
        
        return message;
    }
}
