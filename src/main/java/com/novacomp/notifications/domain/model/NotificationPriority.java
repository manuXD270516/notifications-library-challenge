package com.novacomp.notifications.domain.model;

/**
 * Enum representing the priority level of a notification.
 */
public enum NotificationPriority {
    /**
     * Low priority notification
     */
    LOW,
    
    /**
     * Normal priority notification (default)
     */
    NORMAL,
    
    /**
     * High priority notification
     */
    HIGH,
    
    /**
     * Critical priority notification (immediate delivery required)
     */
    CRITICAL
}
