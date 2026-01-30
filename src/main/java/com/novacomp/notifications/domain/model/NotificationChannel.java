package com.novacomp.notifications.domain.model;

/**
 * Enum representing the available notification channels.
 * This enum defines the types of channels through which notifications can be sent.
 */
public enum NotificationChannel {
    /**
     * Email notification channel
     */
    EMAIL,
    
    /**
     * SMS notification channel
     */
    SMS,
    
    /**
     * Push notification channel for mobile devices
     */
    PUSH,
    
    /**
     * Slack messaging channel
     */
    SLACK
}
