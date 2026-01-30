package com.novacomp.notifications.domain.port;

import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;

/**
 * Port interface for the notification sending service.
 * This is the main entry point for sending notifications.
 */
public interface NotificationSenderPort {
    
    /**
     * Sends a notification through the appropriate channel
     * 
     * @param request the notification request
     * @return the result of the sending operation
     */
    NotificationResult send(NotificationRequest request);
}
