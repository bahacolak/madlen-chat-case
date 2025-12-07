package com.madlen.chat.service.impl;

import com.madlen.chat.dto.AuthResponse;
import com.madlen.chat.dto.LoginRequest;
import com.madlen.chat.dto.RegisterRequest;
import com.madlen.chat.exception.BadRequestException;
import com.madlen.chat.exception.ResourceNotFoundException;
import com.madlen.chat.exception.UnauthorizedException;
import com.madlen.chat.model.User;
import com.madlen.chat.security.JwtTokenProvider;
import com.madlen.chat.service.AuthService;
import com.madlen.chat.service.UserService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final Tracer tracer;

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider, Tracer tracer) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.tracer = tracer;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        Span span = tracer.spanBuilder("auth.register")
                .setAttribute("username", request.getUsername())
                .setAttribute("email", request.getEmail())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            if (userService.existsByUsername(request.getUsername())) {
                BadRequestException ex = new BadRequestException("Username already exists");
                span.recordException(ex);
                throw ex;
            }

            if (userService.existsByEmail(request.getEmail())) {
                BadRequestException ex = new BadRequestException("Email already exists");
                span.recordException(ex);
                throw ex;
            }

            User user = userService.createUser(request.getUsername(), request.getEmail(), request.getPassword());
            String token = tokenProvider.generateToken(user.getUsername());

            span.setAttribute("user.id", user.getId());
            span.setAttribute("success", true);

            return new AuthResponse(token, new AuthResponse.UserDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()));
        } catch (Exception e) {
            span.recordException(e);
            span.setAttribute("success", false);
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Span span = tracer.spanBuilder("auth.login")
                .setAttribute("username", request.getUsername())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            User user;
            try {
                user = userService.findByUsername(request.getUsername());
            } catch (ResourceNotFoundException e) {
                UnauthorizedException ex = new UnauthorizedException("Invalid username or password");
                span.recordException(ex);
                span.setAttribute("success", false);
                throw ex;
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                UnauthorizedException ex = new UnauthorizedException("Invalid username or password");
                span.recordException(ex);
                span.setAttribute("success", false);
                throw ex;
            }

            String token = tokenProvider.generateToken(user.getUsername());

            span.setAttribute("user.id", user.getId());
            span.setAttribute("success", true);

            return new AuthResponse(token, new AuthResponse.UserDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()));
        } catch (Exception e) {
            span.recordException(e);
            span.setAttribute("success", false);
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public void logout(String token) {
        tokenProvider.invalidateToken(token);
    }
}
