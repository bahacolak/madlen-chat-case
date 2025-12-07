package com.madlen.chat.service;

public interface TokenCacheService {
    void cacheToken(String token);
    boolean isTokenCached(String token);
    void invalidateToken(String token);
}
