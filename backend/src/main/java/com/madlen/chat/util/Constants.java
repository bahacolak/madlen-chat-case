package com.madlen.chat.util;

/**
 * Application-wide constants
 */
public final class Constants {
    
    private Constants() {
        // Utility class - prevent instantiation
    }
    
    public static final String DEFAULT_CONVERSATION_TITLE = "New Conversation";
    public static final int MAX_TITLE_LENGTH = 50;
    public static final String IMAGE_DATA_PREFIX = "data:image/jpeg;base64,";
    public static final String TEST_MESSAGE_PREFIX = "/test ";
    
    public static final String TITLE_ELLIPSIS = "...";
}
