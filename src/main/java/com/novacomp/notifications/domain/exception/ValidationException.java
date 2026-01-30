package com.novacomp.notifications.domain.exception;

/**
 * Exception thrown when notification request validation fails.
 */
public class ValidationException extends NotificationException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
