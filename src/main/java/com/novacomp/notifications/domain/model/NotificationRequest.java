package com.novacomp.notifications.domain.model;

import com.novacomp.notifications.domain.exception.ValidationException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an immutable notification request with all necessary information to send a notification.
 * This is a record (Java 14+) providing immutability, equals/hashCode, and toString by default.
 * 
 * <p>Uses Java 21 features:
 * <ul>
 *   <li>Record for immutable data modeling</li>
 *   <li>Compact constructor for validation</li>
 *   <li>Optional API for null-safe operations</li>
 *   <li>Objects.requireNonNullElse for defaults</li>
 * </ul>
 * 
 * @param channel The channel through which the notification should be sent
 * @param recipient The recipient (email address, phone number, device token, etc.)
 * @param subject The subject/title of the notification (mainly used for emails)
 * @param message The main message content
 * @param metadata Optional metadata for additional channel-specific parameters
 * @param priority Priority level of the notification (defaults to NORMAL)
 */
public record NotificationRequest(
    NotificationChannel channel,
    String recipient,
    String subject,
    String message,
    Map<String, Object> metadata,
    NotificationPriority priority
) {
    
    /**
     * Compact constructor with validation.
     * Automatically validates all fields and applies defaults.
     * 
     * @throws ValidationException if validation fails
     */
    public NotificationRequest {
        // Validate and normalize inputs using modern Java features
        channel = Objects.requireNonNull(channel, "Channel cannot be null");
        
        recipient = Optional.ofNullable(recipient)
            .map(String::trim)
            .filter(r -> !r.isEmpty())
            .orElseThrow(() -> new ValidationException("Recipient is required and cannot be blank"));
        
        message = Optional.ofNullable(message)
            .map(String::trim)
            .filter(m -> !m.isEmpty())
            .orElseThrow(() -> new ValidationException("Message is required and cannot be blank"));
        
        // Subject can be null for some channels (SMS, Push)
        subject = Optional.ofNullable(subject)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .orElse(null);
        
        // Metadata is optional, default to empty map
        metadata = Objects.requireNonNullElse(metadata, Map.of());
        
        // Priority defaults to NORMAL
        priority = Objects.requireNonNullElse(priority, NotificationPriority.NORMAL);
    }
    
    /**
     * Builder-style constructor for backward compatibility.
     * Provides a fluent API similar to Lombok's @Builder.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a new request with the specified channel.
     * Demonstrates functional API design with immutable updates.
     * 
     * @param newChannel the new channel
     * @return a new NotificationRequest with the updated channel
     */
    public NotificationRequest withChannel(NotificationChannel newChannel) {
        return new NotificationRequest(newChannel, recipient, subject, message, metadata, priority);
    }
    
    /**
     * Creates a new request with the specified priority.
     * 
     * @param newPriority the new priority
     * @return a new NotificationRequest with the updated priority
     */
    public NotificationRequest withPriority(NotificationPriority newPriority) {
        return new NotificationRequest(channel, recipient, subject, message, metadata, newPriority);
    }
    
    /**
     * Checks if this request has a subject.
     * Uses Optional API for elegant null handling.
     * 
     * @return true if subject is present and not empty
     */
    public boolean hasSubject() {
        return Optional.ofNullable(subject)
            .filter(s -> !s.isEmpty())
            .isPresent();
    }
    
    /**
     * Gets the subject as an Optional.
     * Modern API design avoiding null returns.
     * 
     * @return Optional containing the subject, or empty if not present
     */
    public Optional<String> subjectOptional() {
        return Optional.ofNullable(subject);
    }
    
    /**
     * Gets a metadata value by key.
     * Type-safe metadata access with Optional.
     * 
     * @param key the metadata key
     * @return Optional containing the value, or empty if not present
     */
    public Optional<Object> getMetadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }
    
    /**
     * Checks if metadata contains a specific key.
     * 
     * @param key the key to check
     * @return true if the key exists in metadata
     */
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    /**
     * Builder class for fluent API construction.
     * Provides the same experience as Lombok's @Builder.
     */
    public static final class Builder {
        private NotificationChannel channel;
        private String recipient;
        private String subject;
        private String message;
        private Map<String, Object> metadata;
        private NotificationPriority priority;
        
        private Builder() {}
        
        public Builder channel(NotificationChannel channel) {
            this.channel = channel;
            return this;
        }
        
        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }
        
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder priority(NotificationPriority priority) {
            this.priority = priority;
            return this;
        }
        
        /**
         * Builds the NotificationRequest.
         * Validation happens in the record's compact constructor.
         * 
         * @return a new NotificationRequest instance
         * @throws ValidationException if validation fails
         */
        public NotificationRequest build() {
            return new NotificationRequest(channel, recipient, subject, message, metadata, priority);
        }
    }
}
