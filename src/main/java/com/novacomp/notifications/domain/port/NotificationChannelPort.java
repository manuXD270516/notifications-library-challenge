package com.novacomp.notifications.domain.port;

import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;

/**
 * Port interface for notification channels (Strategy Pattern).
 * Each notification channel (Email, SMS, Push, etc.) implements this interface.
 * This follows the Dependency Inversion Principle from SOLID.
 */
public interface NotificationChannelPort {
    
    /**
     * Sends a notification through this channel
     * 
     * @param request the notification request
     * @return the result of the sending operation
     */
    NotificationResult send(NotificationRequest request);
    
    /**
     * Gets the type of this notification channel
     * 
     * @return the channel type
     */
    NotificationChannel getChannelType();
    
    /**
     * Checks if this channel supports the given request
     * 
     * @param request the notification request
     * @return true if supported, false otherwise
     */
    default boolean supports(NotificationRequest request) {
        return request != null && request.getChannel() == getChannelType();
    }
    
    /**
     * Validates the notification request for this specific channel
     * 
     * @param request the notification request
     * @throws com.novacomp.notifications.domain.exception.ValidationException if validation fails
     */
    void validate(NotificationRequest request);
}
