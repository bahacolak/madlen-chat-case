package com.madlen.chat.util;

import com.madlen.chat.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthenticationHelper {
    
    private AuthenticationHelper() {
    }
    
    public static Long getUserIdFromAuthentication(Authentication authentication, UserService userService) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.findByUsername(username).getId();
    }
}
