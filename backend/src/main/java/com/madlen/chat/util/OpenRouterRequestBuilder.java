package com.madlen.chat.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OpenRouterRequestBuilder {
    
    private OpenRouterRequestBuilder() {
    }
    
    public static Map<String, Object> buildChatRequest(
            String message,
            String model,
            List<Map<String, String>> history,
            String imageBase64,
            boolean stream) {
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        if (stream) {
            requestBody.put("stream", true);
        }
        
        List<Map<String, Object>> requestMessages = new ArrayList<>();
        
        if (history != null) {
            for (Map<String, String> msg : history) {
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("role", msg.get("role"));
                msgMap.put("content", msg.get("content"));
                requestMessages.add(msgMap);
            }
        }
        
        Map<String, Object> currentMessage = new HashMap<>();
        currentMessage.put("role", OpenRouterConstants.ROLE_USER);
        
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            List<Map<String, Object>> contentList = new ArrayList<>();
            contentList.add(Map.of(
                    "type", OpenRouterConstants.CONTENT_TYPE_TEXT,
                    "text", message));
            contentList.add(Map.of(
                    "type", OpenRouterConstants.CONTENT_TYPE_IMAGE_URL,
                    "image_url", Map.of("url", Constants.IMAGE_DATA_PREFIX + imageBase64)));
            currentMessage.put("content", contentList);
        } else {
            currentMessage.put("content", message);
        }
        
        requestMessages.add(currentMessage);
        requestBody.put("messages", requestMessages);
        
        return requestBody;
    }
}
