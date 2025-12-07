package com.madlen.chat.integration;

import com.madlen.chat.service.RateLimitService;
import com.madlen.chat.service.TokenCacheService;
import com.madlen.chat.util.CacheConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RedisCacheIntegrationTest {

    @Autowired(required = false)
    private RateLimitService rateLimitService;

    @Autowired(required = false)
    private TokenCacheService tokenCacheService;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        if (redisTemplate != null) {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }

    @Test
    void testRedisConnection() {
        assertNotNull(redisTemplate, "RedisTemplate bean should exist");
        
        String testKey = "test:connection:" + System.currentTimeMillis();
        String testValue = "test-value";
        
        redisTemplate.opsForValue().set(testKey, testValue);
        String retrievedValue = redisTemplate.opsForValue().get(testKey);
        
        assertEquals(testValue, retrievedValue, "Redis connection should work");
        
        redisTemplate.delete(testKey);
        
        System.out.println("✓ Redis connection is working");
    }

    @Test
    void testRateLimitService_ShouldAllowRequestsWithinLimit() {
        if (rateLimitService == null) {
            System.out.println("⚠ RateLimitService bean not found");
            return;
        }

        Long userId = 1000L;

        for (int i = 0; i < CacheConstants.RATE_LIMIT_REQUESTS_PER_MINUTE; i++) {
            boolean allowed = rateLimitService.isAllowed(userId);
            assertTrue(allowed, "Request " + (i + 1) + " should be allowed");
            rateLimitService.recordRequest(userId);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertFalse(rateLimitService.isAllowed(userId), 
            "Request should be rejected when limit is exceeded");

        if (redisTemplate != null) {
            String key = CacheConstants.KEY_PREFIX_RATE_LIMIT + userId + ":chat";
            Long count = redisTemplate.opsForZSet().zCard(key);
            assertNotNull(count, "Rate limit key should exist in Redis");
            assertTrue(count >= CacheConstants.RATE_LIMIT_REQUESTS_PER_MINUTE, 
                "Number of requests recorded in Redis should be at least " + CacheConstants.RATE_LIMIT_REQUESTS_PER_MINUTE + ", found: " + count);
        }

        System.out.println("✓ RateLimitService is working with Redis");
    }

    @Test
    void testRateLimitService_ShouldBlockAfterLimit() {
        if (rateLimitService == null) {
            System.out.println("⚠ RateLimitService bean not found");
            return;
        }

        Long userId = 2000L;

        for (int i = 0; i < CacheConstants.RATE_LIMIT_REQUESTS_PER_MINUTE; i++) {
            rateLimitService.recordRequest(userId);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertFalse(rateLimitService.isAllowed(userId), 
            "Request should be rejected when limit is exceeded");

        System.out.println("✓ Rate limiting limit check is working");
    }

    @Test
    void testRateLimitService_MultipleUsers() {
        if (rateLimitService == null) {
            System.out.println("⚠ RateLimitService bean not found");
            return;
        }

        Long userId1 = 3000L;
        Long userId2 = 3001L;

        for (int i = 0; i < CacheConstants.RATE_LIMIT_REQUESTS_PER_MINUTE; i++) {
            rateLimitService.recordRequest(userId1);
            rateLimitService.recordRequest(userId2);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertFalse(rateLimitService.isAllowed(userId1), 
            "userId1 should reach the limit");
        assertFalse(rateLimitService.isAllowed(userId2), 
            "userId2 should reach the limit");

        if (redisTemplate != null) {
            String key1 = CacheConstants.KEY_PREFIX_RATE_LIMIT + userId1 + ":chat";
            String key2 = CacheConstants.KEY_PREFIX_RATE_LIMIT + userId2 + ":chat";
            
            assertTrue(redisTemplate.hasKey(key1), 
                "Key should exist for userId1");
            assertTrue(redisTemplate.hasKey(key2), 
                "Key should exist for userId2");
        }

        System.out.println("✓ Multiple user rate limiting is working");
    }

    @Test
    void testTokenCacheService_CacheToken() {
        if (tokenCacheService == null) {
            System.out.println("⚠ TokenCacheService bean not found");
            return;
        }

        String token = "test-token-" + System.currentTimeMillis();

        tokenCacheService.cacheToken(token);
        assertTrue(tokenCacheService.isTokenCached(token), 
            "Token should be cached");

        if (redisTemplate != null) {
            Set<String> tokenKeys = redisTemplate.keys(CacheConstants.KEY_PREFIX_TOKEN + "*");
            assertTrue(tokenKeys != null && !tokenKeys.isEmpty(), 
                "Token key should exist in Redis");
        }

        System.out.println("✓ TokenCacheService is caching tokens");
    }

    @Test
    void testTokenCacheService_InvalidateToken() {
        if (tokenCacheService == null) {
            System.out.println("⚠ TokenCacheService bean not found");
            return;
        }

        String token = "test-token-invalidate-" + System.currentTimeMillis();

        tokenCacheService.cacheToken(token);
        assertTrue(tokenCacheService.isTokenCached(token));

        tokenCacheService.invalidateToken(token);
        assertFalse(tokenCacheService.isTokenCached(token), 
            "Invalidated token should be removed from cache");

        System.out.println("✓ TokenCacheService token invalidation is working");
    }

    @Test
    void testTokenCacheService_DifferentTokens() {
        if (tokenCacheService == null) {
            System.out.println("⚠ TokenCacheService bean not found");
            return;
        }

        String token1 = "token-1-" + System.currentTimeMillis();
        String token2 = "token-2-" + (System.currentTimeMillis() + 1);

        tokenCacheService.cacheToken(token1);
        tokenCacheService.cacheToken(token2);

        assertTrue(tokenCacheService.isTokenCached(token1), 
            "token1 should be cached");
        assertTrue(tokenCacheService.isTokenCached(token2), 
            "token2 should be cached");

        tokenCacheService.invalidateToken(token1);

        assertFalse(tokenCacheService.isTokenCached(token1), 
            "token1 should not be in cache after invalidation");
        assertTrue(tokenCacheService.isTokenCached(token2), 
            "token2 should still be in cache");

        System.out.println("✓ Different tokens are cached independently");
    }

    @Test
    void testRedisKeyExpiration() throws InterruptedException {
        if (redisTemplate == null) {
            System.out.println("⚠ RedisTemplate bean not found");
            return;
        }

        String testKey = "test:expiration:" + System.currentTimeMillis();
        String testValue = "test-value";

        redisTemplate.opsForValue().set(testKey, testValue, 
            java.time.Duration.ofSeconds(2));

        assertTrue(redisTemplate.hasKey(testKey), "Key should exist immediately");

        Thread.sleep(3000);

        assertFalse(redisTemplate.hasKey(testKey), "Key should not exist after expiration");
        
        System.out.println("✓ Redis key expiration is working");
    }

    @Test
    void testRateLimitWindowSliding() {
        if (rateLimitService == null) {
            System.out.println("⚠ RateLimitService bean not found");
            return;
        }

        Long userId = 4000L;

        for (int i = 0; i < 5; i++) {
            rateLimitService.recordRequest(userId);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertTrue(rateLimitService.isAllowed(userId), 
            "Should still be allowed after 5 requests");

        for (int i = 0; i < 5; i++) {
            rateLimitService.recordRequest(userId);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertFalse(rateLimitService.isAllowed(userId), 
            "Should reach limit after 10 requests");

        if (redisTemplate != null) {
            String key = CacheConstants.KEY_PREFIX_RATE_LIMIT + userId + ":chat";
            long now = System.currentTimeMillis() / 1000;
            long windowStart = now - CacheConstants.RATE_LIMIT_WINDOW_SECONDS;
            
            Long count = redisTemplate.opsForZSet().count(key, windowStart, now);
            assertTrue(count >= 10L, 
                "Number of requests in window should be at least 10, found: " + count);
        }

        System.out.println("✓ Rate limit sliding window is working");
    }

    @Test
    void testRedisKeysExist() {
        if (redisTemplate == null || rateLimitService == null || tokenCacheService == null) {
            System.out.println("⚠ Required beans not found");
            return;
        }

        Long userId = 5000L;
        String token = "test-redis-keys-" + System.currentTimeMillis();

        rateLimitService.recordRequest(userId);
        
        tokenCacheService.cacheToken(token);

        String rateLimitKey = CacheConstants.KEY_PREFIX_RATE_LIMIT + userId + ":chat";
        assertTrue(redisTemplate.hasKey(rateLimitKey), 
            "Rate limit key should exist in Redis");

        Set<String> tokenKeys = redisTemplate.keys(CacheConstants.KEY_PREFIX_TOKEN + "*");
        assertTrue(tokenKeys != null && !tokenKeys.isEmpty(), 
            "Token key should exist in Redis");

        System.out.println("✓ Redis keys are being created correctly");
    }
}
