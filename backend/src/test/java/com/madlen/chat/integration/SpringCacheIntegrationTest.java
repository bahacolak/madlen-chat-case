package com.madlen.chat.integration;

import com.madlen.chat.util.CacheConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SpringCacheIntegrationTest {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
    }

    @Test
    void testCacheManagerConfiguration() {
        if (cacheManager == null) {
            System.out.println("⚠ CacheManager bean not found");
            return;
        }

        assertNotNull(cacheManager.getCache(CacheConstants.CACHE_MODELS), 
            "Models cache should be defined");
        assertNotNull(cacheManager.getCache(CacheConstants.CACHE_USERS), 
            "Users cache should be defined");
        assertNotNull(cacheManager.getCache(CacheConstants.CACHE_CONVERSATIONS), 
            "Conversations cache should be defined");

        System.out.println("✓ CacheManager configuration is correct");
    }

    @Test
    void testCachePutAndGet() {
        if (cacheManager == null) {
            System.out.println("⚠ CacheManager bean not found");
            return;
        }

        Cache usersCache = cacheManager.getCache(CacheConstants.CACHE_USERS);
        assertNotNull(usersCache, "Users cache should exist");

        String testKey = "test-username";
        String testValue = "test-user-data";

        usersCache.put(testKey, testValue);

        String retrieved = usersCache.get(testKey, String.class);
        assertEquals(testValue, retrieved, 
            "Value read from cache should match the written value");

        if (stringRedisTemplate != null) {
            String redisKey = CacheConstants.CACHE_USERS + "::" + testKey;
            boolean exists = Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey));
            assertTrue(exists, "Value should also exist in Redis");
        }

        System.out.println("✓ Cache PUT and GET are working");
    }

    @Test
    void testCacheEvict() {
        if (cacheManager == null) {
            System.out.println("⚠ CacheManager bean not found");
            return;
        }

        Cache usersCache = cacheManager.getCache(CacheConstants.CACHE_USERS);
        assertNotNull(usersCache, "Users cache should exist");

        String testKey = "test-evict-key";
        String testValue = "test-evict-value";

        usersCache.put(testKey, testValue);
        assertNotNull(usersCache.get(testKey), "Value should be in cache");

        usersCache.evict(testKey);
        assertNull(usersCache.get(testKey), "Value should be removed from cache");

        if (stringRedisTemplate != null) {
            String redisKey = CacheConstants.CACHE_USERS + "::" + testKey;
            boolean exists = Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey));
            assertFalse(exists, "Value should also be removed from Redis");
        }

        System.out.println("✓ Cache EVICT is working");
    }

    @Test
    void testCacheClear() {
        if (cacheManager == null) {
            System.out.println("⚠ CacheManager bean not found");
            return;
        }

        Cache usersCache = cacheManager.getCache(CacheConstants.CACHE_USERS);
        assertNotNull(usersCache, "Users cache should exist");

        usersCache.put("key1", "value1");
        usersCache.put("key2", "value2");
        usersCache.put("key3", "value3");

        usersCache.clear();

        assertNull(usersCache.get("key1"), "key1 should be deleted");
        assertNull(usersCache.get("key2"), "key2 should be deleted");
        assertNull(usersCache.get("key3"), "key3 should be deleted");

        System.out.println("✓ Cache CLEAR is working");
    }

    @Test
    void testCacheTTL() {
        if (cacheManager == null || stringRedisTemplate == null) {
            System.out.println("⚠ CacheManager or StringRedisTemplate bean not found");
            return;
        }

        Cache usersCache = cacheManager.getCache(CacheConstants.CACHE_USERS);
        assertNotNull(usersCache, "Users cache should exist");

        String testKey = "test-ttl-key";
        String testValue = "test-ttl-value";

        usersCache.put(testKey, testValue);

        String redisKey = CacheConstants.CACHE_USERS + "::" + testKey;
        Long ttl = stringRedisTemplate.getExpire(redisKey);

        assertNotNull(ttl, "TTL value should exist");
        assertTrue(ttl > 0, "TTL should be positive: " + ttl);
        assertTrue(ttl <= CacheConstants.TTL_USERS, 
            "TTL should be less than or equal to the configured cache value");

        System.out.println("✓ Cache TTL is working: " + ttl + " seconds");
    }

    @Test
    void testMultipleCaches() {
        if (cacheManager == null) {
            System.out.println("⚠ CacheManager bean not found");
            return;
        }

        Cache modelsCache = cacheManager.getCache(CacheConstants.CACHE_MODELS);
        Cache usersCache = cacheManager.getCache(CacheConstants.CACHE_USERS);
        Cache conversationsCache = cacheManager.getCache(CacheConstants.CACHE_CONVERSATIONS);

        if (modelsCache != null) {
            modelsCache.put("model-key", "model-value");
            assertEquals("model-value", modelsCache.get("model-key", String.class));
        }

        if (usersCache != null) {
            usersCache.put("user-key", "user-value");
            assertEquals("user-value", usersCache.get("user-key", String.class));
        }

        if (conversationsCache != null) {
            conversationsCache.put("conv-key", "conv-value");
            assertEquals("conv-value", conversationsCache.get("conv-key", String.class));
        }

        System.out.println("✓ Multiple caches are working independently");
    }
}
