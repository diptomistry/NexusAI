package com.example.ai_assistant_backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 * Handles prepared statement conflicts and other database-related errors
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PreparedStatementConflictException.class)
    public ResponseEntity<Map<String, Object>> handlePreparedStatementConflict(
            PreparedStatementConflictException ex, WebRequest request) {

        logger.warn("Prepared statement conflict detected: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Database Connection Conflict");
        response.put("message", "A database connection conflict occurred. Please try again.");
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(org.postgresql.util.PSQLException.class)
    public ResponseEntity<Map<String, Object>> handlePostgreSQLException(
            org.postgresql.util.PSQLException ex, WebRequest request) {

        logger.error("PostgreSQL error: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Database Error");

        // Check if it's a prepared statement conflict
        if (ex.getMessage() != null && ex.getMessage().contains("prepared statement")) {
            response.put("message", "Database connection conflict. Please try again in a moment.");
            response.put("retryable", true);
        } else {
            response.put("message", "A database error occurred. Please contact support if the issue persists.");
            response.put("retryable", false);
        }

        response.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred. Please try again.");
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
