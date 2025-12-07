package com.madlen.chat.service.impl;

import com.madlen.chat.service.RateLimitService;
import com.madlen.chat.util.CacheConstants;
import com.madlen.chat.util.CacheHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitServiceImpl.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimitServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(Long userId) {
        try {
            String key = CacheHelper.getRateLimitKey(userId);
            long now = Instant.now().getEpochSecond();
            long windowStart = now - CacheConstants.RATE_LIMIT_WINDOW_SECONDS;

            Set<String> requests = redisTemplate.opsForZSet().rangeByScore(key, windowStart, now);
            return requests == null || requests.size() < CacheConstants.RATE_LIMIT_REQUESTS_PER_MINUTE;
        } catch (Exception e) {
            logger.warn("Rate limit check failed, allowing request: {}", e.getMessage());
            return true;
        }
    }

    @Override
    public void recordRequest(Long userId) {
        try {
            String key = CacheHelper.getRateLimitKey(userId);
            long now = Instant.now().getEpochSecond();
            String member = String.valueOf(now) + ":" + System.nanoTime();
            redisTemplate.opsForZSet().add(key, member, now);
            redisTemplate.expire(key, java.time.Duration.ofSeconds(CacheConstants.RATE_LIMIT_WINDOW_SECONDS + 10));
        } catch (Exception e) {
            logger.warn("Failed to record rate limit request: {}", e.getMessage());
        }
    }
}
