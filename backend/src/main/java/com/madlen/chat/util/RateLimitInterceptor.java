package com.madlen.chat.util;

import com.madlen.chat.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Rate limiting kontrolü için interceptor.
 * Kullanıcı başına dakikada 10 istek limiti uygular.
 * 
 * NOT: Bu bir case study projesi olduğu için Fail-Open Pattern kullanılıyor.
 * Exception durumunda request geçiyor, bu production'da güvenlik riski oluşturur.
 * 
 * Fail-Open Pattern: Servis çalışmazsa istekler geçer (kullanıcı deneyimi öncelikli)
 * Fail-Secure Pattern: Servis çalışmazsa istekler engellenir (güvenlik öncelikli)
 * 
 * Production'da Fail-Secure Pattern kullanılmalıdır.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitService rateLimitService;
    private final com.madlen.chat.service.UserService userService;

    public RateLimitInterceptor(RateLimitService rateLimitService,
                                com.madlen.chat.service.UserService userService) {
        this.rateLimitService = rateLimitService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getUserPrincipal() instanceof Authentication authentication) {
            try {
                String username = ((UserDetails) authentication.getPrincipal()).getUsername();
                Long userId = userService.findByUsername(username).getId();

                if (!rateLimitService.isAllowed(userId)) {
                    response.setStatus(429); // SC_TOO_MANY_REQUESTS
                    response.setContentType("application/json");
                    try {
                        response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
                    } catch (IOException e) {
                        logger.error("Failed to write rate limit response", e);
                    }
                    return false;
                }

                rateLimitService.recordRequest(userId);
            } catch (Exception e) {
                // NOT: Fail-Open Pattern kullanılıyor (case study için)
                // Exception durumunda request geçiyor - bu güvenlik riski oluşturur.
                // 
                // Sorun: Rate limiting servisi çalışmazsa (Redis down, network error, vs.)
                // tüm istekler geçiyor ve rate limiting bypass edilebiliyor.
                // 
                // Production'da şu şekilde düzeltilmeli:
                // 
                // 1. Fail-Secure Pattern (Önerilen):
                //    - Exception durumunda request'i engelle
                //    - Rate limiting servisi çalışmıyorsa güvenlik öncelikli olmalı
                //    logger.error("Rate limit check failed, blocking request for security", e);
                //    response.setStatus(503); // Service Unavailable
                //    return false;
                // 
                // 2. Veya en azından log seviyesini ERROR'a yükselt:
                //    logger.error("Rate limit check failed, allowing request: {}", e.getMessage(), e);
                //    (Mevcut kod: logger.debug - çok düşük seviye, production'da görülmez)
                logger.debug("Rate limit check failed, allowing request: {}", e.getMessage());
            }
        }
        return true;
    }
}
