package com.novacomp.notifications.application.service;

import com.novacomp.notifications.domain.exception.ConfigurationException;
import com.novacomp.notifications.domain.exception.ValidationException;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import com.novacomp.notifications.domain.port.NotificationChannelPort;
import com.novacomp.notifications.domain.port.NotificationSenderPort;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Main notification service that orchestrates sending notifications through different channels.
 * This service acts as a facade and uses the Strategy Pattern to delegate to appropriate channels.
 * 
 * <p>This class implements the Application Service pattern from Clean Architecture.
 * 
 * <p>Enhanced with Java 21 features:
 * <ul>
 *   <li><b>ConcurrentHashMap</b>: Thread-safe channel registry</li>
 *   <li><b>Stream API</b>: Functional collection processing</li>
 *   <li><b>Optional API</b>: Null-safe channel retrieval</li>
 *   <li><b>Method References</b>: Concise lambda expressions</li>
 * </ul>
 */
@Slf4j
public class NotificationService implements NotificationSenderPort {
    
    // ConcurrentHashMap for thread-safe operations
    private final Map<NotificationChannel, NotificationChannelPort> channels;
    
    /**
     * Creates a new NotificationService with no channels registered.
     * Uses ConcurrentHashMap for thread-safe channel management.
     * Channels must be registered using {@link #registerChannel(NotificationChannelPort)}
     */
    public NotificationService() {
        this.channels = new ConcurrentHashMap<>();
    }
    
    /**
     * Registers a notification channel for use.
     * Thread-safe operation using ConcurrentHashMap.
     * 
     * @param channel the channel to register
     * @throws ConfigurationException if channel is null
     */
    public void registerChannel(NotificationChannelPort channel) {
        Optional.ofNullable(channel)
            .map(NotificationChannelPort::getChannelType)
            .ifPresentOrElse(
                channelType -> {
                    var previous = channels.put(channelType, channel);
                    if (previous != null) {
                        log.warn("Overwriting existing channel: {}", channelType);
                    }
                    log.info("Registered notification channel: {}", channelType);
                },
                () -> { throw new ConfigurationException("Channel cannot be null"); }
            );
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
        log.debug("Sending notification through channel: {}", request.channel());
        
        // Validate request (record constructor already validates, we just check null)
        validateRequest(request);
        
        // Get the appropriate channel using Optional API
        NotificationChannelPort channel = findChannel(request.channel())
            .orElseThrow(() -> new ConfigurationException(
                "Channel not registered: " + request.channel() + 
                ". Please register the channel before sending notifications."
            ));
        
        // Validate request for specific channel
        channel.validate(request);
        
        // Send notification
        try {
            NotificationResult result = channel.send(request);
            
            // Use functional API for result handling
            result.ifSuccess(messageId -> 
                log.info("Notification sent successfully through {}: messageId={}",
                    request.channel(), messageId)
            ).ifFailure(error -> 
                log.error("Failed to send notification through {}: {}",
                    request.channel(), error)
            );
            
            return result;
            
        } catch (Exception e) {
            log.error("Error sending notification through {}", request.channel(), e);
            return NotificationResult.failure(request.channel(), e);
        }
    }
    
    /**
     * Sends notifications to multiple channels sequentially.
     * Demonstrates Stream API for multi-channel operations.
     * 
     * @param request the base notification request
     * @param channelTypes the channels to send through
     * @return map of channel to result
     */
    public Map<NotificationChannel, NotificationResult> sendToMultipleChannels(
        NotificationRequest request,
        Set<NotificationChannel> channelTypes
    ) {
        log.info("Sending notification to {} channels", channelTypes.size());
        
        return channelTypes.stream()
            .map(channel -> request.withChannel(channel))
            .collect(Collectors.toMap(
                NotificationRequest::channel,
                this::send,
                (r1, r2) -> r1, // merge function (shouldn't be needed)
                LinkedHashMap::new // maintain order
            ));
    }
    
    /**
     * Gets all registered channel types.
     * Stream API returning immutable sorted list.
     * 
     * @return sorted list of registered channels
     */
    public List<NotificationChannel> getRegisteredChannels() {
        return channels.keySet()
            .stream()
            .sorted()
            .toList(); // Java 16+: immutable list
    }
    
    /**
     * Finds a channel by type using Optional API.
     * Demonstrates modern null-safe retrieval.
     * 
     * @param channelType the channel type
     * @return Optional containing the channel, or empty if not found
     */
    public Optional<NotificationChannelPort> findChannel(NotificationChannel channelType) {
        return Optional.ofNullable(channels.get(channelType));
    }
    
    /**
     * Checks if all specified channels are registered.
     * Stream API for validation.
     * 
     * @param channelTypes channels to check
     * @return true if all are registered
     */
    public boolean areAllChannelsRegistered(Collection<NotificationChannel> channelTypes) {
        return channelTypes.stream()
            .allMatch(this::isChannelRegistered);
    }
    
    /**
     * Gets channels that support a specific request.
     * Stream API with filtering.
     * 
     * @param request the notification request
     * @return list of channels that support the request
     */
    public List<NotificationChannelPort> findSupportingChannels(NotificationRequest request) {
        return channels.values()
            .stream()
            .filter(channel -> channel.supports(request))
            .toList();
    }
    
    /**
     * Gets statistics about registered channels.
     * Demonstrates Collectors.groupingBy for data aggregation.
     * 
     * @return map of channel type to channel name
     */
    public Map<String, Long> getChannelStatistics() {
        return channels.values()
            .stream()
            .collect(Collectors.groupingBy(
                channel -> channel.getChannelType().name(),
                Collectors.counting()
            ));
    }
    
    /**
     * Validates the basic notification request.
     * The record's compact constructor already performs validation,
     * so this method primarily ensures non-null.
     * 
     * @param request the request to validate
     * @throws ValidationException if validation fails
     */
    private void validateRequest(NotificationRequest request) {
        // Record constructor already validates, but we check null here
        if (request == null) {
            throw new ValidationException("Notification request cannot be null");
        }
        // All other validation is done in the record's compact constructor
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
