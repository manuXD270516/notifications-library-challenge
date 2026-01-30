package com.novacomp.notifications.application.service;

import com.novacomp.notifications.domain.exception.ConfigurationException;
import com.novacomp.notifications.domain.exception.ValidationException;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import com.novacomp.notifications.domain.port.NotificationChannelPort;
import com.novacomp.notifications.domain.port.NotificationSenderPort;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Main notification service that orchestrates sending notifications through different channels.
 * This service acts as a facade and uses the Strategy Pattern to delegate to appropriate channels.
 * 
 * This class implements the Application Service pattern from Clean Architecture.
 */
@Slf4j
public class NotificationService implements NotificationSenderPort {
    
    private final Map<NotificationChannel, NotificationChannelPort> channels;
    
    /**
     * Creates a new NotificationService with no channels registered.
     * Channels must be registered using {@link #registerChannel(NotificationChannelPort)}
     */
    public NotificationService() {
        this.channels = new HashMap<>();
    }
    
    /**
     * Registers a notification channel for use.
     * 
     * @param channel the channel to register
     * @throws ConfigurationException if channel is null
     */
    public void registerChannel(NotificationChannelPort channel) {
        if (channel == null) {
            throw new ConfigurationException("Channel cannot be null");
        }
        
        NotificationChannel channelType = channel.getChannelType();
        if (channels.containsKey(channelType)) {
            log.warn("Overwriting existing channel: {}", channelType);
        }
        
        channels.put(channelType, channel);
        log.info("Registered notification channel: {}", channelType);
    }
    
    /**
     * Sends a notification through the appropriate channel.
     * 
     * @param request the notification request
     * @return the result of the sending operation
     * @throws ValidationException if the request is invalid
     * @throws ConfigurationException if the requested channel is not registered
     */
    @Override
    public NotificationResult send(NotificationRequest request) {
        log.debug("Sending notification through channel: {}", request.getChannel());
        
        // Validate request
        validateRequest(request);
        
        // Get the appropriate channel
        NotificationChannelPort channel = getChannel(request.getChannel());
        
        // Validate request for specific channel
        channel.validate(request);
        
        // Send notification
        try {
            NotificationResult result = channel.send(request);
            
            if (result.isSuccess()) {
                log.info("Notification sent successfully through {}: messageId={}",
                    request.getChannel(), result.getMessageId());
            } else {
                log.error("Failed to send notification through {}: {}",
                    request.getChannel(), result.getErrorMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error sending notification through {}", request.getChannel(), e);
            return NotificationResult.failure(request.getChannel(), e);
        }
    }
    
    /**
     * Validates the basic notification request.
     * 
     * @param request the request to validate
     * @throws ValidationException if validation fails
     */
    private void validateRequest(NotificationRequest request) {
        if (request == null) {
            throw new ValidationException("Notification request cannot be null");
        }
        
        if (!request.isValid()) {
            throw new ValidationException(
                "Invalid notification request: missing required fields"
            );
        }
    }
    
    /**
     * Gets the channel for the specified type.
     * 
     * @param channelType the channel type
     * @return the channel
     * @throws ConfigurationException if channel is not registered
     */
    private NotificationChannelPort getChannel(NotificationChannel channelType) {
        NotificationChannelPort channel = channels.get(channelType);
        
        if (channel == null) {
            throw new ConfigurationException(
                "Channel not registered: " + channelType + 
                ". Please register the channel before sending notifications."
            );
        }
        
        return channel;
    }
    
    /**
     * Checks if a channel is registered.
     * 
     * @param channelType the channel type to check
     * @return true if registered, false otherwise
     */
    public boolean isChannelRegistered(NotificationChannel channelType) {
        return channels.containsKey(channelType);
    }
    
    /**
     * Gets the number of registered channels.
     * 
     * @return the number of channels
     */
    public int getRegisteredChannelsCount() {
        return channels.size();
    }
}
