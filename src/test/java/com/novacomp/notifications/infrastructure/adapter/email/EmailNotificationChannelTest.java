package com.novacomp.notifications.infrastructure.adapter.email;

import com.novacomp.notifications.domain.exception.ConfigurationException;
import com.novacomp.notifications.domain.exception.ValidationException;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import com.novacomp.notifications.infrastructure.config.EmailConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailNotificationChannel.
 */
@DisplayName("EmailNotificationChannel Tests")
class EmailNotificationChannelTest {
    
    @Test
    @DisplayName("Should create email channel with SendGrid configuration")
    void shouldCreateEmailChannelWithSendGridConfiguration() {
        // Given
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .apiKey("test-api-key")
            .from("noreply@example.com")
            .fromName("Test App")
            .build();
        
        // When
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        
        // Then
        assertNotNull(channel);
        assertEquals(NotificationChannel.EMAIL, channel.getChannelType());
    }
    
    @Test
    @DisplayName("Should create email channel with Mailgun configuration")
    void shouldCreateEmailChannelWithMailgunConfiguration() {
        // Given
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.MAILGUN)
            .apiKey("test-api-key")
            .from("noreply@example.com")
            .build();
        
        // When
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        
        // Then
        assertNotNull(channel);
        assertEquals(NotificationChannel.EMAIL, channel.getChannelType());
    }
    
    @Test
    @DisplayName("Should create email channel with SMTP configuration")
    void shouldCreateEmailChannelWithSmtpConfiguration() {
        // Given
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SMTP)
            .from("noreply@example.com")
            .smtpHost("smtp.example.com")
            .smtpPort(587)
            .smtpUsername("user@example.com")
            .smtpPassword("password")
            .useTls(true)
            .build();
        
        // When
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        
        // Then
        assertNotNull(channel);
        assertEquals(NotificationChannel.EMAIL, channel.getChannelType());
    }
    
    @Test
    @DisplayName("Should throw exception when config is null")
    void shouldThrowExceptionWhenConfigIsNull() {
        // When & Then
        assertThrows(ConfigurationException.class, () -> {
            new EmailNotificationChannel(null);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when config is invalid")
    void shouldThrowExceptionWhenConfigIsInvalid() {
        // Given
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .from("noreply@example.com")
            // Missing API key
            .build();
        
        // When & Then
        assertThrows(ConfigurationException.class, () -> {
            new EmailNotificationChannel(config);
        });
    }
    
    @Test
    @DisplayName("Should send email successfully")
    void shouldSendEmailSuccessfully() {
        // Given
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .apiKey("test-api-key")
            .from("noreply@example.com")
            .build();
        
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .subject("Test Subject")
            .message("Test message")
            .build();
        
        // When
        NotificationResult result = channel.send(request);
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(NotificationChannel.EMAIL, result.channel());
        assertTrue(result.messageId().isPresent());
        assertTrue(result.messageId().get().startsWith("sendgrid-"));
    }
    
    @Test
    @DisplayName("Should validate email address format")
    void shouldValidateEmailAddressFormat() {
        // Given
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .apiKey("test-api-key")
            .from("noreply@example.com")
            .build();
        
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("invalid-email")
            .subject("Test")
            .message("Test message")
            .build();
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            channel.validate(request);
        });
    }
    
    @Test
    @DisplayName("Should validate subject is required")
    void shouldValidateSubjectIsRequired() {
        // Given
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .apiKey("test-api-key")
            .from("noreply@example.com")
            .build();
        
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .message("Test message")
            .build();
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            channel.validate(request);
        });
    }
    
    @Test
    @DisplayName("Should support email request")
    void shouldSupportEmailRequest() {
        // Given
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .apiKey("test-api-key")
            .from("noreply@example.com")
            .build();
        
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        // When & Then
        assertTrue(channel.supports(request));
    }
    
    @Test
    @DisplayName("Should not support non-email request")
    void shouldNotSupportNonEmailRequest() {
        // Given
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .apiKey("test-api-key")
            .from("noreply@example.com")
            .build();
        
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.SMS)
            .recipient("+1234567890")
            .message("Test message")
            .build();
        
        // When & Then
        assertFalse(channel.supports(request));
    }
}
