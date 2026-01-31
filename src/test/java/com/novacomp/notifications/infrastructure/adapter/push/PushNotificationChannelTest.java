package com.novacomp.notifications.infrastructure.adapter.push;

import com.novacomp.notifications.domain.exception.ConfigurationException;
import com.novacomp.notifications.domain.exception.ValidationException;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationPriority;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import com.novacomp.notifications.infrastructure.config.PushConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PushNotificationChannel.
 */
@DisplayName("PushNotificationChannel Tests")
class PushNotificationChannelTest {
    
    @Test
    @DisplayName("Should create push channel with FCM configuration")
    void shouldCreatePushChannelWithFcmConfiguration() {
        // Given
        PushConfig config = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            .serverKey("test-server-key")
            .projectId("test-project-id")
            .build();
        
        // When
        PushNotificationChannel channel = new PushNotificationChannel(config);
        
        // Then
        assertNotNull(channel);
        assertEquals(NotificationChannel.PUSH, channel.getChannelType());
    }
    
    @Test
    @DisplayName("Should throw exception when config is null")
    void shouldThrowExceptionWhenConfigIsNull() {
        // When & Then
        assertThrows(ConfigurationException.class, () -> {
            new PushNotificationChannel(null);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when config is invalid")
    void shouldThrowExceptionWhenConfigIsInvalid() {
        // Given
        PushConfig config = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            // Missing server key
            .build();
        
        // When & Then
        assertThrows(ConfigurationException.class, () -> {
            new PushNotificationChannel(config);
        });
    }
    
    @Test
    @DisplayName("Should send push notification successfully")
    void shouldSendPushNotificationSuccessfully() {
        // Given
        PushConfig config = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            .serverKey("test-server-key")
            .build();
        
        PushNotificationChannel channel = new PushNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.PUSH)
            .recipient("device-token-123456789012345678901234567890")
            .subject("Test Notification")
            .message("Test message")
            .priority(NotificationPriority.HIGH)
            .build();
        
        // When
        NotificationResult result = channel.send(request);
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(NotificationChannel.PUSH, result.channel());
        assertTrue(result.messageId().isPresent());
        assertTrue(result.messageId().get().startsWith("fcm-"));
    }
    
    @Test
    @DisplayName("Should validate device token is required")
    void shouldValidateDeviceTokenIsRequired() {
        // Given
        PushConfig config = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            .serverKey("test-server-key")
            .build();
        
        PushNotificationChannel channel = new PushNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.PUSH)
            .recipient("")
            .message("Test message")
            .build();
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            channel.validate(request);
        });
    }
    
    @Test
    @DisplayName("Should validate device token format")
    void shouldValidateDeviceTokenFormat() {
        // Given
        PushConfig config = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            .serverKey("test-server-key")
            .build();
        
        PushNotificationChannel channel = new PushNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.PUSH)
            .recipient("short-token")
            .message("Test message")
            .build();
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            channel.validate(request);
        });
    }
    
    @Test
    @DisplayName("Should accept valid device token")
    void shouldAcceptValidDeviceToken() {
        // Given
        PushConfig config = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            .serverKey("test-server-key")
            .build();
        
        PushNotificationChannel channel = new PushNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.PUSH)
            .recipient("device-token-123456789012345678901234567890")
            .subject("Test")
            .message("Test message")
            .build();
        
        // When & Then
        assertDoesNotThrow(() -> channel.validate(request));
    }
    
    @Test
    @DisplayName("Should support push request")
    void shouldSupportPushRequest() {
        // Given
        PushConfig config = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            .serverKey("test-server-key")
            .build();
        
        PushNotificationChannel channel = new PushNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.PUSH)
            .recipient("device-token-123456789012345678901234567890")
            .message("Test message")
            .build();
        
        // When & Then
        assertTrue(channel.supports(request));
    }
}
