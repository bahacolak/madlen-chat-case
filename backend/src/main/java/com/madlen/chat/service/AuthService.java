package com.madlen.chat.service;

import com.madlen.chat.dto.AuthResponse;
import com.madlen.chat.dto.LoginRequest;
import com.madlen.chat.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}

