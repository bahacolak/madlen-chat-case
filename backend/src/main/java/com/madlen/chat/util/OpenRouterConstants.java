package com.madlen.chat.util;

public final class OpenRouterConstants {
    
    private OpenRouterConstants() {
    }
    
    public static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";
    public static final String MODELS_ENDPOINT = "/models";
    
    public static final String HEADER_HTTP_REFERER = "HTTP-Referer";
    public static final String HEADER_X_TITLE = "X-Title";
    
    public static final String DEFAULT_HTTP_REFERER = "http://localhost:8080";
    public static final String DEFAULT_X_TITLE = "Chat Application";
    
    public static final String STREAM_DONE_MARKER = "[DONE]";
    public static final String SSE_DATA_PREFIX = "data: ";
    
    public static final String ROLE_USER = "user";
    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_IMAGE_URL = "image_url";
    public static final String CONTENT_TYPE_IMAGE = "image";
}
