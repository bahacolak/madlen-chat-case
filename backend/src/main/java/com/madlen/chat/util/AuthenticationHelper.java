package com.madlen.chat.util;

import com.madlen.chat.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility class for authentication-related operations
 */
public final class AuthenticationHelper {
    
    private AuthenticationHelper() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Extracts user ID from Spring Security Authentication object
     * 
     * @param authentication The Spring Security Authentication object
     * @param userService Service for user operations
     * @return The user ID
     */
    public static Long getUserIdFromAuthentication(Authentication authentication, UserService userService) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.findByUsername(username).getId();
    }
}
