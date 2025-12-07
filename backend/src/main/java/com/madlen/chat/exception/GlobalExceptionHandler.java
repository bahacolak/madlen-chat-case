package com.madlen.chat.exception;

import com.madlen.chat.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler - Tüm exception'ları merkezi olarak handle eder.
 * 
 * NOT: Bu bir case study projesi olduğu için bazı exception'larda detaylı error message
 * expose ediliyor. Production ortamında Information Disclosure riski oluşturabilir.
 * 
 * Güvenlik Best Practices:
 * - Generic exception'larda sensitive bilgi gönderme
 * - Stack trace'leri client'a gönderme
 * - Database error'larını sanitize et
 * - File path'leri, connection string'leri gizle
 * - Detaylı error bilgisi sadece log'larda olmalı
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                "Invalid username or password",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                "Invalid username or password",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * OpenRouter API exception'larını handle eder.
     * 
     * NOT: External service error mesajı direkt expose ediliyor (case study için).
     * Production'da API key bilgileri veya internal error detayları sızabilir.
     * Generic mesaj kullanılmalı: "External service temporarily unavailable"
     */
    @ExceptionHandler(OpenRouterException.class)
    public ResponseEntity<ErrorResponse> handleOpenRouterException(
            OpenRouterException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "External Service Error",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Validation errors: " + errors.toString(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Tüm yakalanmamış exception'ları handle eder.
     * 
     * NOT: Bu bir case study projesi olduğu için exception message direkt expose ediliyor.
     * Production ortamında güvenlik riski oluşturur çünkü:
     * - Stack trace bilgileri sızabilir (dosya yolları, class isimleri, line number'lar)
     * - Database connection string'leri, API key'leri gibi sensitive bilgiler görünebilir
     * - Saldırganlar sistem mimarisi hakkında bilgi toplayabilir
     * - Internal error detayları saldırı yüzeyini genişletebilir
     * 
     * Production'da şu şekilde düzeltilmeli:
     * 
     * // Generic error mesajı kullan (sensitive bilgi sızıntısını önle)
     * String errorMessage = "An unexpected error occurred";
     * 
     * // Veya environment-based mesaj:
     * String errorMessage = isProduction() 
     *     ? "An unexpected error occurred" 
     *     : ex.getMessage(); // Development'ta detaylı mesaj göster
     * 
     * ErrorResponse error = new ErrorResponse(
     *     HttpStatus.INTERNAL_SERVER_ERROR.value(),
     *     "Internal Server Error",
     *     errorMessage,  // Sanitize edilmiş mesaj
     *     request.getRequestURI());
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception occurred: {} - {}", 
                ex.getClass().getName(), 
                ex.getMessage(), 
                ex);
        
        if (ex.getCause() != null) {
            logger.error("Caused by: {}", ex.getCause().getMessage());
        }

        // NOT: Production'da ex.getMessage() yerine generic mesaj kullanılmalı
        // Detaylı error bilgisi sadece log'larda olmalı, client'a gönderilmemeli
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
