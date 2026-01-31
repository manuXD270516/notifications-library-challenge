package com.novacomp.notifications.infrastructure.adapter.push;

import com.novacomp.notifications.domain.exception.ConfigurationException;
import com.novacomp.notifications.domain.exception.SendingException;
import com.novacomp.notifications.domain.exception.ValidationException;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import com.novacomp.notifications.domain.port.NotificationChannelPort;
import com.novacomp.notifications.infrastructure.config.PushConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Push notification channel implementation.
 * Supports multiple push providers: Firebase Cloud Messaging (FCM).
 * 
 * This is an Adapter in the Ports and Adapters (Hexagonal) architecture.
 */
@Slf4j
public class PushNotificationChannel implements NotificationChannelPort {
    
    private final PushConfig config;
    private final PushSender pushSender;
    
    /**
     * Creates a new push notification channel with the given configuration.
     * 
     * @param config the push configuration
     * @throws ConfigurationException if configuration is invalid
     */
    public PushNotificationChannel(PushConfig config) {
        if (config == null) {
            throw new ConfigurationException("Push configuration cannot be null");
        }
        
        if (!config.isValid()) {
            throw new ConfigurationException("Invalid push configuration");
        }
        
        this.config = config;
        this.pushSender = createPushSender(config);
        
        log.info("Push notification channel initialized with provider: {}", 
            config.getProvider());
    }
    
    /**
     * Creates the appropriate push sender based on the provider.
     * Factory Method pattern.
     * 
     * @param config the push configuration
     * @return the push sender
     */
    private PushSender createPushSender(PushConfig config) {
        switch (config.getProvider()) {
            case FCM:
                return new FcmPushSender(config);
            default:
                throw new ConfigurationException(
                    "Unsupported push provider: " + config.getProvider()
                );
        }
    }
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        log.debug("Sending push notification to device: {}", request.recipient());
        
        try {
            // Create push message
            PushMessage message = PushMessage.builder()
                .deviceToken(request.recipient())
                .title(request.subject())
                .body(request.message())
                .priority(mapPriority(request.priority()))
                .build();
            
            // Send push notification
            String messageId = pushSender.send(message);
            
            log.info("Push notification sent successfully to device: {}, messageId: {}", 
                request.recipient(), messageId);
            
            return NotificationResult.success(NotificationChannel.PUSH, messageId);
            
        } catch (Exception e) {
            log.error("Failed to send push notification to device: {}", 
                request.recipient(), e);
            throw new SendingException("Failed to send push notification", e);
        }
    }
    
    @Override
    public NotificationChannel getChannelType() {
        return NotificationChannel.PUSH;
    }
    
    @Override
    public void validate(NotificationRequest request) {
        if (request.recipient() == null || request.recipient().trim().isEmpty()) {
            throw new ValidationException("Device token is required for push notifications");
        }
        
        if (request.recipient().length() < 20) {
            throw new ValidationException("Invalid device token format");
        }
    }
    
    /**
     * Maps domain priority to push message priority.
     * 
     * @param priority the domain priority
     * @return the push message priority
     */
    private PushMessage.Priority mapPriority(
        com.novacomp.notifications.domain.model.NotificationPriority priority
    ) {
        if (priority == null) {
            return PushMessage.Priority.NORMAL;
        }
        
        switch (priority) {
            case LOW:
                return PushMessage.Priority.LOW;
            case HIGH:
            case CRITICAL:
                return PushMessage.Priority.HIGH;
            default:
                return PushMessage.Priority.NORMAL;
        }
    }
}
