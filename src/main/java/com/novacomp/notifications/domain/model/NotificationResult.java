package com.novacomp.notifications.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents the result of a notification sending operation.
 * Contains information about success/failure and any relevant details.
 */
@Data
@Builder
public class NotificationResult {
    /**
     * Indicates whether the notification was sent successfully
     */
    private boolean success;
    
    /**
     * The channel through which the notification was sent
     */
    private NotificationChannel channel;
    
    /**
     * Message ID or tracking identifier from the provider (if available)
     */
    private String messageId;
    
    /**
     * Error message if the operation failed
     */
    private String errorMessage;
    
    /**
     * Exception that caused the failure (if any)
     */
    private Throwable error;
    
    /**
     * Timestamp when the operation completed
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Creates a successful result
     * 
     * @param channel the channel used
     * @param messageId the message ID from provider
     * @return a successful NotificationResult
     */
    public static NotificationResult success(NotificationChannel channel, String messageId) {
        return NotificationResult.builder()
            .success(true)
            .channel(channel)
            .messageId(messageId)
            .build();
    }
    
    /**
     * Creates a failed result
     * 
     * @param channel the channel used
     * @param errorMessage the error message
     * @return a failed NotificationResult
     */
    public static NotificationResult failure(NotificationChannel channel, String errorMessage) {
        return NotificationResult.builder()
            .success(false)
            .channel(channel)
            .errorMessage(errorMessage)
            .build();
    }
    
    /**
     * Creates a failed result with exception
     * 
     * @param channel the channel used
     * @param error the exception that caused the failure
     * @return a failed NotificationResult
     */
    public static NotificationResult failure(NotificationChannel channel, Throwable error) {
        return NotificationResult.builder()
            .success(false)
            .channel(channel)
            .errorMessage(error.getMessage())
            .error(error)
            .build();
    }
}
