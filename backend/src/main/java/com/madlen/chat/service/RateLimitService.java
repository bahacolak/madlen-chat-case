package com.madlen.chat.service;

public interface RateLimitService {
    boolean isAllowed(Long userId);
    void recordRequest(Long userId);
}
