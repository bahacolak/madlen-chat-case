package com.madlen.chat.service;

import com.madlen.chat.model.User;

public interface UserService {
    User findByUsername(String username);
    User createUser(String username, String email, String password);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

