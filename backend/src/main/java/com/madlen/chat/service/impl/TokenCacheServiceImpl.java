package com.madlen.chat.service.impl;

import com.madlen.chat.service.TokenCacheService;
import com.madlen.chat.util.CacheConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Duration;

@Service
public class TokenCacheServiceImpl implements TokenCacheService {

    private static final Logger logger = LoggerFactory.getLogger(TokenCacheServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final long jwtExpiration;

    public TokenCacheServiceImpl(StringRedisTemplate redisTemplate,
                                 @Value("${spring.security.jwt.expiration}") long jwtExpiration) {
        this.redisTemplate = redisTemplate;
        this.jwtExpiration = jwtExpiration;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return token.substring(0, Math.min(32, token.length()));
        }
    }

    @Override
    public void cacheToken(String token) {
        try {
            String tokenHash = hashToken(token);
            String key = CacheConstants.KEY_PREFIX_TOKEN + tokenHash;
            redisTemplate.opsForValue().set(key, "valid", Duration.ofMillis(jwtExpiration));
        } catch (Exception e) {
            logger.warn("Failed to cache token: {}", e.getMessage());
        }
    }

    @Override
    public boolean isTokenCached(String token) {
        try {
            String tokenHash = hashToken(token);
            String key = CacheConstants.KEY_PREFIX_TOKEN + tokenHash;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.warn("Failed to check token cache: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void invalidateToken(String token) {
        try {
            String tokenHash = hashToken(token);
            String key = CacheConstants.KEY_PREFIX_TOKEN + tokenHash;
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.warn("Failed to invalidate token: {}", e.getMessage());
        }
    }
}
