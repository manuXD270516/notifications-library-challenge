package com.novacomp.notifications.infrastructure.adapter.sms;

import com.novacomp.notifications.domain.exception.ConfigurationException;
import com.novacomp.notifications.domain.exception.ValidationException;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import com.novacomp.notifications.infrastructure.config.SmsConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SmsNotificationChannel.
 */
@DisplayName("SmsNotificationChannel Tests")
class SmsNotificationChannelTest {
    
    @Test
    @DisplayName("Should create SMS channel with Twilio configuration")
    void shouldCreateSmsChannelWithTwilioConfiguration() {
        // Given
        SmsConfig config = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .accountSid("test-account-sid")
            .authToken("test-auth-token")
            .fromNumber("+1234567890")
            .build();
        
        // When
        SmsNotificationChannel channel = new SmsNotificationChannel(config);
        
        // Then
        assertNotNull(channel);
        assertEquals(NotificationChannel.SMS, channel.getChannelType());
    }
    
    @Test
    @DisplayName("Should throw exception when config is null")
    void shouldThrowExceptionWhenConfigIsNull() {
        // When & Then
        assertThrows(ConfigurationException.class, () -> {
            new SmsNotificationChannel(null);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when config is invalid")
    void shouldThrowExceptionWhenConfigIsInvalid() {
        // Given
        SmsConfig config = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .fromNumber("+1234567890")
            // Missing account SID and auth token
            .build();
        
        // When & Then
        assertThrows(ConfigurationException.class, () -> {
            new SmsNotificationChannel(config);
        });
    }
    
    @Test
    @DisplayName("Should send SMS successfully")
    void shouldSendSmsSuccessfully() {
        // Given
        SmsConfig config = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .accountSid("test-account-sid")
            .authToken("test-auth-token")
            .fromNumber("+1234567890")
            .build();
        
        SmsNotificationChannel channel = new SmsNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.SMS)
            .recipient("+19876543210")
            .message("Test message")
            .build();
        
        // When
        NotificationResult result = channel.send(request);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(NotificationChannel.SMS, result.getChannel());
        assertNotNull(result.getMessageId());
        assertTrue(result.getMessageId().startsWith("twilio-"));
    }
    
    @Test
    @DisplayName("Should validate phone number format")
    void shouldValidatePhoneNumberFormat() {
        // Given
        SmsConfig config = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .accountSid("test-account-sid")
            .authToken("test-auth-token")
            .fromNumber("+1234567890")
            .build();
        
        SmsNotificationChannel channel = new SmsNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.SMS)
            .recipient("invalid-phone")
            .message("Test message")
            .build();
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            channel.validate(request);
        });
    }
    
    @Test
    @DisplayName("Should validate message length")
    void shouldValidateMessageLength() {
        // Given
        SmsConfig config = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .accountSid("test-account-sid")
            .authToken("test-auth-token")
            .fromNumber("+1234567890")
            .build();
        
        SmsNotificationChannel channel = new SmsNotificationChannel(config);
        
        // Create a message longer than 1600 characters
        String longMessage = "x".repeat(1601);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.SMS)
            .recipient("+19876543210")
            .message(longMessage)
            .build();
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            channel.validate(request);
        });
    }
    
    @Test
    @DisplayName("Should accept valid E.164 phone number")
    void shouldAcceptValidE164PhoneNumber() {
        // Given
        SmsConfig config = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .accountSid("test-account-sid")
            .authToken("test-auth-token")
            .fromNumber("+1234567890")
            .build();
        
        SmsNotificationChannel channel = new SmsNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.SMS)
            .recipient("+19876543210")
            .message("Test message")
            .build();
        
        // When & Then
        assertDoesNotThrow(() -> channel.validate(request));
    }
    
    @Test
    @DisplayName("Should support SMS request")
    void shouldSupportSmsRequest() {
        // Given
        SmsConfig config = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .accountSid("test-account-sid")
            .authToken("test-auth-token")
            .fromNumber("+1234567890")
            .build();
        
        SmsNotificationChannel channel = new SmsNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.SMS)
            .recipient("+19876543210")
            .message("Test message")
            .build();
        
        // When & Then
        assertTrue(channel.supports(request));
    }
}
