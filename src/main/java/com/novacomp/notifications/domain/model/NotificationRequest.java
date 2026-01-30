package com.novacomp.notifications.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Represents a notification request with all necessary information to send a notification.
 * This is the main domain model for notification operations.
 */
@Data
@Builder
public class NotificationRequest {
    /**
     * The channel through which the notification should be sent
     */
    private NotificationChannel channel;
    
    /**
     * The recipient of the notification (email address, phone number, device token, etc.)
     */
    private String recipient;
    
    /**
     * The subject/title of the notification (mainly used for emails)
     */
    private String subject;
    
    /**
     * The main message content
     */
    private String message;
    
    /**
     * Optional metadata for additional channel-specific parameters
     */
    private Map<String, Object> metadata;
    
    /**
     * Priority level of the notification
     */
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    /**
     * Validates the notification request
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return channel != null 
            && recipient != null && !recipient.trim().isEmpty()
            && message != null && !message.trim().isEmpty();
    }
}
