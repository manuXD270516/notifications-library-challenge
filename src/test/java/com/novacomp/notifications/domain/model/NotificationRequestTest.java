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
        assertEquals(NotificationChannel.EMAIL, request.channel());
        assertEquals("user@example.com", request.recipient());
        assertEquals("Test Subject", request.subject());
        assertEquals("Test message", request.message());
        assertEquals(NotificationPriority.HIGH, request.priority());
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
        assertEquals(NotificationPriority.NORMAL, request.priority());
    }
    
    @Test
    @DisplayName("Should throw exception when channel is null")
    void shouldThrowExceptionWhenChannelIsNull() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> {
            NotificationRequest.builder()
                .recipient("user@example.com")
                .message("Test message")
                .build();
        });
    }
    
    @Test
    @DisplayName("Should throw exception when recipient is null")
    void shouldThrowExceptionWhenRecipientIsNull() {
        // Given & When & Then
        assertThrows(Exception.class, () -> {
            NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .message("Test message")
                .build();
        });
    }
    
    @Test
    @DisplayName("Should throw exception when recipient is empty")
    void shouldThrowExceptionWhenRecipientIsEmpty() {
        // Given & When & Then
        assertThrows(Exception.class, () -> {
            NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("   ")
                .message("Test message")
                .build();
        });
    }
    
    @Test
    @DisplayName("Should throw exception when message is null")
    void shouldThrowExceptionWhenMessageIsNull() {
        // Given & When & Then
        assertThrows(Exception.class, () -> {
            NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .build();
        });
    }
    
    @Test
    @DisplayName("Should throw exception when message is empty")
    void shouldThrowExceptionWhenMessageIsEmpty() {
        // Given & When & Then
        assertThrows(Exception.class, () -> {
            NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .message("   ")
                .build();
        });
    }
}
