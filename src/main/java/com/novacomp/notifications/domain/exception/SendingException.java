package com.novacomp.notifications.domain.exception;

/**
 * Exception thrown when sending a notification fails.
 */
public class SendingException extends NotificationException {
    
    public SendingException(String message) {
        super(message);
    }
    
    public SendingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SendingException(Throwable cause) {
        super(cause);
    }
}
