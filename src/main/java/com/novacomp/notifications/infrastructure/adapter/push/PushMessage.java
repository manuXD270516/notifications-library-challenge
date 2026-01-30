package com.novacomp.notifications.infrastructure.adapter.push;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Represents a push notification message with all necessary information.
 * This is an internal model used by push senders.
 */
@Data
@Builder
public class PushMessage {
    /**
     * Device token to send the notification to
     */
    private String deviceToken;
    
    /**
     * Notification title
     */
    private String title;
    
    /**
     * Notification body
     */
    private String body;
    
    /**
     * Priority level
     */
    @Builder.Default
    private Priority priority = Priority.NORMAL;
    
    /**
     * Additional data to send with the notification
     */
    private Map<String, String> data;
    
    /**
     * Priority levels for push notifications
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH
    }
}
