package com.novacomp.notifications.application.service;

import com.novacomp.notifications.domain.exception.ConfigurationException;
import com.novacomp.notifications.domain.exception.ValidationException;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import com.novacomp.notifications.domain.port.NotificationChannelPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {
    
    @Mock
    private NotificationChannelPort emailChannel;
    
    @Mock
    private NotificationChannelPort smsChannel;
    
    private NotificationService notificationService;
    
    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        
        // Configure mocks with lenient() to avoid UnnecessaryStubbingException
        lenient().when(emailChannel.getChannelType()).thenReturn(NotificationChannel.EMAIL);
        lenient().when(smsChannel.getChannelType()).thenReturn(NotificationChannel.SMS);
    }
    
    @Test
    @DisplayName("Should register channel successfully")
    void shouldRegisterChannelSuccessfully() {
        // When
        notificationService.registerChannel(emailChannel);
        
        // Then
        assertTrue(notificationService.isChannelRegistered(NotificationChannel.EMAIL));
        assertEquals(1, notificationService.getRegisteredChannelsCount());
    }
    
    @Test
    @DisplayName("Should throw exception when registering null channel")
    void shouldThrowExceptionWhenRegisteringNullChannel() {
        // When & Then
        assertThrows(ConfigurationException.class, () -> {
            notificationService.registerChannel(null);
        });
    }
    
    @Test
    @DisplayName("Should send notification successfully")
    void shouldSendNotificationSuccessfully() {
        // Given
        notificationService.registerChannel(emailChannel);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        NotificationResult expectedResult = NotificationResult.success(
            NotificationChannel.EMAIL, "msg-123"
        );
        
        when(emailChannel.send(any(NotificationRequest.class)))
            .thenReturn(expectedResult);
        doNothing().when(emailChannel).validate(any(NotificationRequest.class));
        
        // When
        NotificationResult result = notificationService.send(request);
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.messageId().isPresent());
        assertEquals("msg-123", result.messageId().get());
        verify(emailChannel).send(request);
        verify(emailChannel).validate(request);
    }
    
    @Test
    @DisplayName("Should throw exception when sending null request")
    void shouldThrowExceptionWhenSendingNullRequest() {
        // When & Then
        assertThrows(ValidationException.class, () -> {
            notificationService.send(null);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when sending invalid request")
    void shouldThrowExceptionWhenSendingInvalidRequest() {
        // When & Then - Record constructor throws ValidationException on invalid data
        assertThrows(ValidationException.class, () -> {
            NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .build(); // Missing required fields - throws on construction
        });
    }
    
    @Test
    @DisplayName("Should throw exception when channel not registered")
    void shouldThrowExceptionWhenChannelNotRegistered() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .message("Test message")
            .build();
        
        // When & Then
        assertThrows(ConfigurationException.class, () -> {
            notificationService.send(request);
        });
    }
    
    @Test
    @DisplayName("Should register multiple channels")
    void shouldRegisterMultipleChannels() {
        // When
        notificationService.registerChannel(emailChannel);
        notificationService.registerChannel(smsChannel);
        
        // Then
        assertTrue(notificationService.isChannelRegistered(NotificationChannel.EMAIL));
        assertTrue(notificationService.isChannelRegistered(NotificationChannel.SMS));
        assertEquals(2, notificationService.getRegisteredChannelsCount());
    }
    
    @Test
    @DisplayName("Should overwrite existing channel when registering again")
    void shouldOverwriteExistingChannelWhenRegisteringAgain() {
        // Given
        notificationService.registerChannel(emailChannel);
        
        NotificationChannelPort anotherEmailChannel = mock(NotificationChannelPort.class);
        when(anotherEmailChannel.getChannelType()).thenReturn(NotificationChannel.EMAIL);
        
        // When
        notificationService.registerChannel(anotherEmailChannel);
        
        // Then
        assertTrue(notificationService.isChannelRegistered(NotificationChannel.EMAIL));
        assertEquals(1, notificationService.getRegisteredChannelsCount());
    }
    
    @Test
    @DisplayName("Should return failure result when channel throws exception")
    void shouldReturnFailureResultWhenChannelThrowsException() {
        // Given
        notificationService.registerChannel(emailChannel);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        doNothing().when(emailChannel).validate(any(NotificationRequest.class));
        when(emailChannel.send(any(NotificationRequest.class)))
            .thenThrow(new RuntimeException("Connection failed"));
        
        // When
        NotificationResult result = notificationService.send(request);
        
        // Then
        assertNotNull(result);
        assertFalse(result.success());
        assertEquals(NotificationChannel.EMAIL, result.channel());
        assertNotNull(result.error());
    }
}
