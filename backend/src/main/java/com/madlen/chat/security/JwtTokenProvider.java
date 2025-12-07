package com.madlen.chat.security;

import com.madlen.chat.service.TokenCacheService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token üretimi, doğrulama ve yönetimi için kullanılan component.
 * 
 * NOT: Bu bir case study projesi olduğu için JWT secret default değer kullanılıyor.
 * Production ortamında güvenlik riski oluşturur çünkü:
 * - Zayıf secret kullanımı token'ların kolayca kırılmasına yol açar
 * - Default secret bilinirse sahte token üretilebilir
 * 
 * Production'da:
 * - JWT_SECRET environment variable zorunlu olmalı
 * - Minimum 256 bit (32 karakter) güçlü secret kullanılmalı
 * - Startup'ta secret kontrolü yapılmalı (@PostConstruct ile)
 */
@Component
public class JwtTokenProvider {
    
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${spring.security.jwt.expiration}")
    private long jwtExpiration;
    
    private final TokenCacheService tokenCacheService;
    
    public JwtTokenProvider(TokenCacheService tokenCacheService) {
        this.tokenCacheService = tokenCacheService;
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
        
        tokenCacheService.cacheToken(token);
        return token;
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            if (!tokenCacheService.isTokenCached(token)) {
                return false;
            }
            
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void invalidateToken(String token) {
        tokenCacheService.invalidateToken(token);
    }
}

