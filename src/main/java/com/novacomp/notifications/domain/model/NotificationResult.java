package com.novacomp.notifications.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents an immutable result of a notification sending operation.
 * This is a record (Java 14+) providing value semantics and immutability.
 * 
 * <p>Uses Java 21 modern features:
 * <ul>
 *   <li>Record for immutable result modeling</li>
 *   <li>Optional API for null-safe error handling</li>
 *   <li>Instant for precise timestamp</li>
 *   <li>Functional API for result processing</li>
 * </ul>
 * 
 * <p>This design follows the Result/Either monad pattern from functional programming.
 * 
 * @param success Indicates whether the notification was sent successfully
 * @param channel The channel through which the notification was sent
 * @param messageId Optional message ID from provider
 * @param errorMessage Optional error message if the operation failed
 * @param error Optional exception that caused the failure
 * @param timestamp Timestamp when the operation completed
 */
public record NotificationResult(
    boolean success,
    NotificationChannel channel,
    Optional<String> messageId,
    Optional<String> errorMessage,
    Optional<Throwable> error,
    Instant timestamp
) {
    
    /**
     * Compact constructor with validation and normalization.
     */
    public NotificationResult {
        Objects.requireNonNull(channel, "Channel cannot be null");
        messageId = Objects.requireNonNull(messageId, "MessageId Optional cannot be null");
        errorMessage = Objects.requireNonNull(errorMessage, "ErrorMessage Optional cannot be null");
        error = Objects.requireNonNull(error, "Error Optional cannot be null");
        timestamp = Objects.requireNonNullElse(timestamp, Instant.now());
    }
    
    /**
     * Creates a successful result.
     * Factory method for type-safe construction.
     * 
     * @param channel the channel used
     * @param messageId the message ID from provider
     * @return a successful NotificationResult
     */
    public static NotificationResult success(NotificationChannel channel, String messageId) {
        return new NotificationResult(
            true,
            channel,
            Optional.ofNullable(messageId),
            Optional.empty(),
            Optional.empty(),
            Instant.now()
        );
    }
    
    /**
     * Creates a failed result with error message.
     * 
     * @param channel the channel used
     * @param errorMessage the error message
     * @return a failed NotificationResult
     */
    public static NotificationResult failure(NotificationChannel channel, String errorMessage) {
        return new NotificationResult(
            false,
            channel,
            Optional.empty(),
            Optional.ofNullable(errorMessage),
            Optional.empty(),
            Instant.now()
        );
    }
    
    /**
     * Creates a failed result with exception.
     * Extracts message from exception using Optional API.
     * 
     * @param channel the channel used
     * @param error the exception that caused the failure
     * @return a failed NotificationResult
     */
    public static NotificationResult failure(NotificationChannel channel, Throwable error) {
        return new NotificationResult(
            false,
            channel,
            Optional.empty(),
            Optional.ofNullable(error).map(Throwable::getMessage),
            Optional.ofNullable(error),
            Instant.now()
        );
    }
    
    /**
     * Checks if this result is a failure.
     * Functional API for result checking.
     * 
     * @return true if operation failed
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * Gets the message ID, throwing if not present.
     * Use this when you're certain the message ID exists.
     * 
     * @return the message ID
     * @throws java.util.NoSuchElementException if message ID is not present
     */
    public String getMessageIdOrThrow() {
        return messageId.orElseThrow(() -> 
            new IllegalStateException("Message ID not present in result"));
    }
    
    /**
     * Executes an action if the result is successful.
     * Functional API for success handling.
     * 
     * @param action the action to execute with the message ID
     * @return this result for chaining
     */
    public NotificationResult ifSuccess(Consumer<String> action) {
        if (success) {
            messageId.ifPresent(action);
        }
        return this;
    }
    
    /**
     * Executes an action if the result is a failure.
     * Functional API for failure handling.
     * 
     * @param action the action to execute with the error message
     * @return this result for chaining
     */
    public NotificationResult ifFailure(Consumer<String> action) {
        if (!success) {
            errorMessage.ifPresent(action);
        }
        return this;
    }
    
    /**
     * Maps this result to another value using a function.
     * Functional transformation of result.
     * 
     * @param successMapper function to apply if successful
     * @param failureMapper function to apply if failed
     * @param <T> the type of the result
     * @return the mapped value
     */
    public <T> T fold(
        Function<String, T> successMapper,
        Function<String, T> failureMapper
    ) {
        return success 
            ? messageId.map(successMapper).orElse(null)
            : errorMessage.map(failureMapper).orElse(null);
    }
    
    /**
     * Recovers from failure with a default message ID.
     * Functional API for error recovery.
     * 
     * @param defaultMessageId the default message ID to use if failed
     * @return message ID or default
     */
    public String recoverWith(String defaultMessageId) {
        return success ? messageId.orElse(defaultMessageId) : defaultMessageId;
    }
    
    /**
     * Creates a string representation suitable for logging.
     * Includes all relevant information based on success/failure.
     * 
     * @return formatted string representation
     */
    public String toLogString() {
        if (success) {
            return String.format(
                "SUCCESS[channel=%s, messageId=%s, timestamp=%s]",
                channel,
                messageId.orElse("N/A"),
                timestamp
            );
        } else {
            return String.format(
                "FAILURE[channel=%s, error=%s, timestamp=%s]",
                channel,
                errorMessage.orElse("Unknown error"),
                timestamp
            );
        }
    }
}
