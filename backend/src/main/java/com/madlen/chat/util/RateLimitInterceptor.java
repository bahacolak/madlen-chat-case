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
                logger.debug("Rate limit check failed, allowing request: {}", e.getMessage());
            }
        }
        return true;
    }
}
