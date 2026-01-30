package com.novacomp.notifications.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationResult.
 */
@DisplayName("NotificationResult Tests")
class NotificationResultTest {
    
    @Test
    @DisplayName("Should create successful result")
    void shouldCreateSuccessfulResult() {
        // Given
        NotificationChannel channel = NotificationChannel.EMAIL;
        String messageId = "msg-123";
        
        // When
        NotificationResult result = NotificationResult.success(channel, messageId);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals(channel, result.getChannel());
        assertEquals(messageId, result.getMessageId());
        assertNull(result.getErrorMessage());
        assertNull(result.getError());
        assertNotNull(result.getTimestamp());
    }
    
    @Test
    @DisplayName("Should create failed result with error message")
    void shouldCreateFailedResultWithErrorMessage() {
        // Given
        NotificationChannel channel = NotificationChannel.SMS;
        String errorMessage = "Failed to send SMS";
        
        // When
        NotificationResult result = NotificationResult.failure(channel, errorMessage);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals(channel, result.getChannel());
        assertEquals(errorMessage, result.getErrorMessage());
        assertNull(result.getMessageId());
        assertNotNull(result.getTimestamp());
    }
    
    @Test
    @DisplayName("Should create failed result with exception")
    void shouldCreateFailedResultWithException() {
        // Given
        NotificationChannel channel = NotificationChannel.PUSH;
        Exception exception = new RuntimeException("Connection timeout");
        
        // When
        NotificationResult result = NotificationResult.failure(channel, exception);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals(channel, result.getChannel());
        assertEquals("Connection timeout", result.getErrorMessage());
        assertEquals(exception, result.getError());
        assertNull(result.getMessageId());
        assertNotNull(result.getTimestamp());
    }
    
    @Test
    @DisplayName("Should create result with builder")
    void shouldCreateResultWithBuilder() {
        // Given & When
        NotificationResult result = NotificationResult.builder()
            .success(true)
            .channel(NotificationChannel.EMAIL)
            .messageId("msg-456")
            .build();
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals(NotificationChannel.EMAIL, result.getChannel());
        assertEquals("msg-456", result.getMessageId());
    }
}
