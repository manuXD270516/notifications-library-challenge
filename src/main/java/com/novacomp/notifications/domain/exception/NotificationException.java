package com.novacomp.notifications.domain.exception;

/**
 * Base exception for all notification-related errors.
 * This is the parent class for all custom exceptions in the notification library.
 */
public class NotificationException extends RuntimeException {
    
    public NotificationException(String message) {
        super(message);
    }
    
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public NotificationException(Throwable cause) {
        super(cause);
    }
}
