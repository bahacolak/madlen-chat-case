package com.madlen.chat.util;

public final class CacheHelper {
    
    private CacheHelper() {
    }
    
    public static String getUserCacheKey(String username) {
        return CacheConstants.KEY_PREFIX_USER_USERNAME + username;
    }
    
    public static String getUserCacheKey(Long userId) {
        return CacheConstants.KEY_PREFIX_USER_ID + userId;
    }
    
    public static String getConversationCacheKey(Long userId) {
        return CacheConstants.KEY_PREFIX_CONVERSATIONS + userId;
    }
    
    public static String getTokenCacheKey(String tokenHash) {
        return CacheConstants.KEY_PREFIX_TOKEN + tokenHash;
    }
    
    public static String getRateLimitKey(Long userId) {
        return CacheConstants.KEY_PREFIX_RATE_LIMIT + userId + ":chat";
    }
}
