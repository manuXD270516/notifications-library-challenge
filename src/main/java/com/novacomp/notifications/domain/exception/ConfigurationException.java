package com.novacomp.notifications.domain.exception;

/**
 * Exception thrown when there's a configuration error.
 */
public class ConfigurationException extends NotificationException {
    
    public ConfigurationException(String message) {
        super(message);
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
