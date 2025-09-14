package com.example.ai_assistant_backend.exception;

/**
 * Custom exception for prepared statement conflicts
 * Used to handle PostgreSQL prepared statement "already exists" errors
 */
public class PreparedStatementConflictException extends RuntimeException {

    public PreparedStatementConflictException(String message) {
        super(message);
    }

    public PreparedStatementConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
