package com.madlen.chat.util;

public final class CacheConstants {
    
    private CacheConstants() {
    }
    
    public static final String CACHE_MODELS = "models";
    public static final String CACHE_USERS = "users";
    public static final String CACHE_CONVERSATIONS = "conversations";
    public static final String CACHE_TOKENS = "tokens";
    
    public static final long TTL_MODELS = 3600;
    public static final long TTL_USERS = 1800;
    public static final long TTL_CONVERSATIONS = 300;
    
    public static final String KEY_PREFIX_USER_USERNAME = "user:username:";
    public static final String KEY_PREFIX_USER_ID = "user:id:";
    public static final String KEY_PREFIX_CONVERSATIONS = "conversations:user:";
    public static final String KEY_PREFIX_TOKEN = "token:";
    public static final String KEY_PREFIX_RATE_LIMIT = "rate_limit:user:";
    
    public static final int RATE_LIMIT_REQUESTS_PER_MINUTE = 10;
    public static final int RATE_LIMIT_WINDOW_SECONDS = 60;
}
