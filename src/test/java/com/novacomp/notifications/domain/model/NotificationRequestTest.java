package com.novacomp.notifications.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationRequest.
 */
@DisplayName("NotificationRequest Tests")
class NotificationRequestTest {
    
    @Test
    @DisplayName("Should create valid notification request")
    void shouldCreateValidNotificationRequest() {
        // Given & When
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .subject("Test Subject")
            .message("Test message")
            .priority(NotificationPriority.HIGH)
            .build();
        
        // Then
        assertNotNull(request);
        assertEquals(NotificationChannel.EMAIL, request.getChannel());
        assertEquals("user@example.com", request.getRecipient());
        assertEquals("Test Subject", request.getSubject());
        assertEquals("Test message", request.getMessage());
        assertEquals(NotificationPriority.HIGH, request.getPriority());
        assertTrue(request.isValid());
    }
    
    @Test
    @DisplayName("Should use default priority when not specified")
    void shouldUseDefaultPriority() {
        // Given & When
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.SMS)
            .recipient("+1234567890")
            .message("Test message")
            .build();
        
        // Then
        assertEquals(NotificationPriority.NORMAL, request.getPriority());
    }
    
    @Test
    @DisplayName("Should be invalid when channel is null")
    void shouldBeInvalidWhenChannelIsNull() {
        // Given & When
        NotificationRequest request = NotificationRequest.builder()
            .recipient("user@example.com")
            .message("Test message")
            .build();
        
        // Then
        assertFalse(request.isValid());
    }
    
    @Test
    @DisplayName("Should be invalid when recipient is null")
    void shouldBeInvalidWhenRecipientIsNull() {
        // Given & When
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .message("Test message")
            .build();
        
        // Then
        assertFalse(request.isValid());
    }
    
    @Test
    @DisplayName("Should be invalid when recipient is empty")
    void shouldBeInvalidWhenRecipientIsEmpty() {
        // Given & When
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("   ")
            .message("Test message")
            .build();
        
        // Then
        assertFalse(request.isValid());
    }
    
    @Test
    @DisplayName("Should be invalid when message is null")
    void shouldBeInvalidWhenMessageIsNull() {
        // Given & When
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .build();
        
        // Then
        assertFalse(request.isValid());
    }
    
    @Test
    @DisplayName("Should be invalid when message is empty")
    void shouldBeInvalidWhenMessageIsEmpty() {
        // Given & When
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .message("   ")
            .build();
        
        // Then
        assertFalse(request.isValid());
    }
}
