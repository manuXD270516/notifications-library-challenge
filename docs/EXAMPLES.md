# Notification Library - Usage Examples

This document provides comprehensive usage examples for the Notifications Library.

## Table of Contents

1. [Basic Email Notification](#basic-email-notification)
2. [SMS Notification](#sms-notification)
3. [Push Notification](#push-notification)
4. [Multiple Channels](#multiple-channels)
5. [Error Handling](#error-handling)
6. [Advanced Configuration](#advanced-configuration)

---

## Basic Email Notification

### SendGrid Example

```java
import com.novacomp.notifications.application.service.NotificationService;
import com.novacomp.notifications.domain.model.*;
import com.novacomp.notifications.infrastructure.adapter.email.EmailNotificationChannel;
import com.novacomp.notifications.infrastructure.config.EmailConfig;

public class SendGridExample {
    public static void main(String[] args) {
        // Create notification service
        NotificationService notificationService = new NotificationService();
        
        // Configure SendGrid email channel
        EmailConfig emailConfig = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .apiKey(System.getenv("SENDGRID_API_KEY"))
            .from("noreply@example.com")
            .fromName("My Application")
            .build();
        
        // Register the email channel
        EmailNotificationChannel emailChannel = new EmailNotificationChannel(emailConfig);
        notificationService.registerChannel(emailChannel);
        
        // Create notification request
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .subject("Welcome to Our Service!")
            .message("Thank you for signing up. We're excited to have you on board!")
            .priority(NotificationPriority.NORMAL)
            .build();
        
        // Send notification
        NotificationResult result = notificationService.send(request);
        
        // Check result
        if (result.isSuccess()) {
            System.out.println("Email sent successfully!");
            System.out.println("Message ID: " + result.getMessageId());
        } else {
            System.err.println("Failed to send email: " + result.getErrorMessage());
        }
    }
}
```

### Mailgun Example

```java
import com.novacomp.notifications.application.service.NotificationService;
import com.novacomp.notifications.domain.model.*;
import com.novacomp.notifications.infrastructure.adapter.email.EmailNotificationChannel;
import com.novacomp.notifications.infrastructure.config.EmailConfig;

public class MailgunExample {
    public static void main(String[] args) {
        NotificationService notificationService = new NotificationService();
        
        // Configure Mailgun
        EmailConfig emailConfig = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.MAILGUN)
            .apiKey(System.getenv("MAILGUN_API_KEY"))
            .from("noreply@yourdomain.com")
            .fromName("Your App")
            .build();
        
        EmailNotificationChannel emailChannel = new EmailNotificationChannel(emailConfig);
        notificationService.registerChannel(emailChannel);
        
        // Send notification
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("customer@example.com")
            .subject("Order Confirmation")
            .message("Your order #12345 has been confirmed and will be shipped soon.")
            .build();
        
        NotificationResult result = notificationService.send(request);
        
        if (result.isSuccess()) {
            System.out.println("✅ Email sent via Mailgun!");
        }
    }
}
```

### SMTP Example

```java
import com.novacomp.notifications.application.service.NotificationService;
import com.novacomp.notifications.domain.model.*;
import com.novacomp.notifications.infrastructure.adapter.email.EmailNotificationChannel;
import com.novacomp.notifications.infrastructure.config.EmailConfig;

public class SmtpExample {
    public static void main(String[] args) {
        NotificationService notificationService = new NotificationService();
        
        // Configure SMTP
        EmailConfig emailConfig = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SMTP)
            .from("noreply@company.com")
            .fromName("Company Name")
            .smtpHost("smtp.gmail.com")
            .smtpPort(587)
            .smtpUsername("your-email@gmail.com")
            .smtpPassword(System.getenv("SMTP_PASSWORD"))
            .useTls(true)
            .build();
        
        EmailNotificationChannel emailChannel = new EmailNotificationChannel(emailConfig);
        notificationService.registerChannel(emailChannel);
        
        // Send notification
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("employee@company.com")
            .subject("Weekly Report")
            .message("Please find attached your weekly performance report.")
            .build();
        
        NotificationResult result = notificationService.send(request);
    }
}
```

---

## SMS Notification

### Twilio Example

```java
import com.novacomp.notifications.application.service.NotificationService;
import com.novacomp.notifications.domain.model.*;
import com.novacomp.notifications.infrastructure.adapter.sms.SmsNotificationChannel;
import com.novacomp.notifications.infrastructure.config.SmsConfig;

public class TwilioSmsExample {
    public static void main(String[] args) {
        NotificationService notificationService = new NotificationService();
        
        // Configure Twilio SMS
        SmsConfig smsConfig = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .accountSid(System.getenv("TWILIO_ACCOUNT_SID"))
            .authToken(System.getenv("TWILIO_AUTH_TOKEN"))
            .fromNumber("+1234567890") // Your Twilio phone number
            .build();
        
        SmsNotificationChannel smsChannel = new SmsNotificationChannel(smsConfig);
        notificationService.registerChannel(smsChannel);
        
        // Send SMS notification
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.SMS)
            .recipient("+19876543210") // Recipient phone number (E.164 format)
            .message("Your verification code is: 123456. Valid for 5 minutes.")
            .priority(NotificationPriority.HIGH)
            .build();
        
        NotificationResult result = notificationService.send(request);
        
        if (result.isSuccess()) {
            System.out.println("📱 SMS sent successfully!");
            System.out.println("Message ID: " + result.getMessageId());
        } else {
            System.err.println("❌ Failed to send SMS: " + result.getErrorMessage());
        }
    }
}
```

---

## Push Notification

### Firebase Cloud Messaging (FCM) Example

```java
import com.novacomp.notifications.application.service.NotificationService;
import com.novacomp.notifications.domain.model.*;
import com.novacomp.notifications.infrastructure.adapter.push.PushNotificationChannel;
import com.novacomp.notifications.infrastructure.config.PushConfig;

public class FcmPushExample {
    public static void main(String[] args) {
        NotificationService notificationService = new NotificationService();
        
        // Configure FCM
        PushConfig pushConfig = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            .serverKey(System.getenv("FCM_SERVER_KEY"))
            .projectId("my-firebase-project")
            .build();
        
        PushNotificationChannel pushChannel = new PushNotificationChannel(pushConfig);
        notificationService.registerChannel(pushChannel);
        
        // Send push notification
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.PUSH)
            .recipient("device-token-from-mobile-app-abc123xyz")
            .subject("New Message")
            .message("You have received a new message from John Doe")
            .priority(NotificationPriority.HIGH)
            .build();
        
        NotificationResult result = notificationService.send(request);
        
        if (result.isSuccess()) {
            System.out.println("🔔 Push notification sent successfully!");
            System.out.println("Message ID: " + result.getMessageId());
        }
    }
}
```

---

## Multiple Channels

### Send to Multiple Channels Example

```java
import com.novacomp.notifications.application.service.NotificationService;
import com.novacomp.notifications.domain.model.*;
import com.novacomp.notifications.infrastructure.adapter.email.EmailNotificationChannel;
import com.novacomp.notifications.infrastructure.adapter.sms.SmsNotificationChannel;
import com.novacomp.notifications.infrastructure.adapter.push.PushNotificationChannel;
import com.novacomp.notifications.infrastructure.config.*;

public class MultiChannelExample {
    public static void main(String[] args) {
        NotificationService notificationService = new NotificationService();
        
        // Configure all channels
        
        // 1. Email (SendGrid)
        EmailConfig emailConfig = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .apiKey(System.getenv("SENDGRID_API_KEY"))
            .from("alerts@company.com")
            .fromName("Company Alerts")
            .build();
        notificationService.registerChannel(new EmailNotificationChannel(emailConfig));
        
        // 2. SMS (Twilio)
        SmsConfig smsConfig = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .accountSid(System.getenv("TWILIO_ACCOUNT_SID"))
            .authToken(System.getenv("TWILIO_AUTH_TOKEN"))
            .fromNumber("+1234567890")
            .build();
        notificationService.registerChannel(new SmsNotificationChannel(smsConfig));
        
        // 3. Push (FCM)
        PushConfig pushConfig = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            .serverKey(System.getenv("FCM_SERVER_KEY"))
            .build();
        notificationService.registerChannel(new PushNotificationChannel(pushConfig));
        
        // Now send notifications through different channels
        
        // Send email
        NotificationResult emailResult = notificationService.send(
            NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .subject("Critical Alert")
                .message("Your account has been accessed from a new location.")
                .priority(NotificationPriority.CRITICAL)
                .build()
        );
        
        // Send SMS
        NotificationResult smsResult = notificationService.send(
            NotificationRequest.builder()
                .channel(NotificationChannel.SMS)
                .recipient("+19876543210")
                .message("Critical Alert: Your account has been accessed from a new location.")
                .priority(NotificationPriority.CRITICAL)
                .build()
        );
        
        // Send push notification
        NotificationResult pushResult = notificationService.send(
            NotificationRequest.builder()
                .channel(NotificationChannel.PUSH)
                .recipient("device-token-abc123xyz")
                .subject("Critical Alert")
                .message("Your account has been accessed from a new location.")
                .priority(NotificationPriority.CRITICAL)
                .build()
        );
        
        // Check results
        System.out.println("Email: " + (emailResult.isSuccess() ? "✅ Sent" : "❌ Failed"));
        System.out.println("SMS: " + (smsResult.isSuccess() ? "✅ Sent" : "❌ Failed"));
        System.out.println("Push: " + (pushResult.isSuccess() ? "✅ Sent" : "❌ Failed"));
    }
}
```

---

## Error Handling

### Comprehensive Error Handling Example

```java
import com.novacomp.notifications.application.service.NotificationService;
import com.novacomp.notifications.domain.exception.*;
import com.novacomp.notifications.domain.model.*;
import com.novacomp.notifications.infrastructure.adapter.email.EmailNotificationChannel;
import com.novacomp.notifications.infrastructure.config.EmailConfig;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        try {
            NotificationService notificationService = new NotificationService();
            
            // Configure email channel
            EmailConfig emailConfig = EmailConfig.builder()
                .provider(EmailConfig.EmailProvider.SENDGRID)
                .apiKey(System.getenv("SENDGRID_API_KEY"))
                .from("noreply@example.com")
                .build();
            
            EmailNotificationChannel emailChannel = new EmailNotificationChannel(emailConfig);
            notificationService.registerChannel(emailChannel);
            
            // Create notification request
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .subject("Test")
                .message("Test message")
                .build();
            
            // Send notification
            NotificationResult result = notificationService.send(request);
            
            // Handle result
            if (result.isSuccess()) {
                System.out.println("✅ Notification sent successfully!");
                System.out.println("Message ID: " + result.getMessageId());
                System.out.println("Sent at: " + result.getTimestamp());
            } else {
                System.err.println("❌ Notification failed!");
                System.err.println("Error: " + result.getErrorMessage());
                
                if (result.getError() != null) {
                    result.getError().printStackTrace();
                }
            }
            
        } catch (ValidationException e) {
            System.err.println("❌ Validation Error: " + e.getMessage());
            // Handle invalid request (missing fields, invalid format, etc.)
            
        } catch (ConfigurationException e) {
            System.err.println("❌ Configuration Error: " + e.getMessage());
            // Handle configuration issues (channel not registered, invalid config, etc.)
            
        } catch (SendingException e) {
            System.err.println("❌ Sending Error: " + e.getMessage());
            // Handle sending failures (network issues, provider errors, etc.)
            
        } catch (NotificationException e) {
            System.err.println("❌ Notification Error: " + e.getMessage());
            // Handle any other notification-related errors
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

---

## Advanced Configuration

### Using Environment Variables

```java
public class EnvironmentConfigExample {
    public static void main(String[] args) {
        NotificationService notificationService = new NotificationService();
        
        // Load configuration from environment variables
        String emailProvider = System.getenv("EMAIL_PROVIDER"); // "SENDGRID", "MAILGUN", or "SMTP"
        
        EmailConfig.EmailConfigBuilder configBuilder = EmailConfig.builder()
            .from(System.getenv("EMAIL_FROM"))
            .fromName(System.getenv("EMAIL_FROM_NAME"));
        
        // Configure based on provider
        switch (emailProvider.toUpperCase()) {
            case "SENDGRID":
                configBuilder
                    .provider(EmailConfig.EmailProvider.SENDGRID)
                    .apiKey(System.getenv("SENDGRID_API_KEY"));
                break;
                
            case "MAILGUN":
                configBuilder
                    .provider(EmailConfig.EmailProvider.MAILGUN)
                    .apiKey(System.getenv("MAILGUN_API_KEY"));
                break;
                
            case "SMTP":
                configBuilder
                    .provider(EmailConfig.EmailProvider.SMTP)
                    .smtpHost(System.getenv("SMTP_HOST"))
                    .smtpPort(Integer.parseInt(System.getenv("SMTP_PORT")))
                    .smtpUsername(System.getenv("SMTP_USERNAME"))
                    .smtpPassword(System.getenv("SMTP_PASSWORD"))
                    .useTls(Boolean.parseBoolean(System.getenv("SMTP_USE_TLS")));
                break;
                
            default:
                throw new IllegalArgumentException("Unknown email provider: " + emailProvider);
        }
        
        EmailConfig emailConfig = configBuilder.build();
        EmailNotificationChannel emailChannel = new EmailNotificationChannel(emailConfig);
        notificationService.registerChannel(emailChannel);
        
        // Use the service...
    }
}
```

### Priority Levels Example

```java
public class PriorityExample {
    public static void main(String[] args) {
        NotificationService notificationService = new NotificationService();
        
        // Configure channels...
        
        // LOW priority - Non-urgent informational messages
        NotificationRequest lowPriority = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .subject("Weekly Newsletter")
            .message("Check out this week's highlights...")
            .priority(NotificationPriority.LOW)
            .build();
        
        // NORMAL priority - Standard notifications (default)
        NotificationRequest normalPriority = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .subject("Order Update")
            .message("Your order has been shipped.")
            .priority(NotificationPriority.NORMAL)
            .build();
        
        // HIGH priority - Important notifications
        NotificationRequest highPriority = NotificationRequest.builder()
            .channel(NotificationChannel.SMS)
            .recipient("+19876543210")
            .message("Your password has been changed. If this wasn't you, contact support immediately.")
            .priority(NotificationPriority.HIGH)
            .build();
        
        // CRITICAL priority - Urgent, time-sensitive notifications
        NotificationRequest criticalPriority = NotificationRequest.builder()
            .channel(NotificationChannel.PUSH)
            .recipient("device-token-abc123")
            .subject("Security Alert")
            .message("Suspicious login attempt detected from unknown device.")
            .priority(NotificationPriority.CRITICAL)
            .build();
        
        // Send all notifications
        notificationService.send(lowPriority);
        notificationService.send(normalPriority);
        notificationService.send(highPriority);
        notificationService.send(criticalPriority);
    }
}
```

---

## Best Practices

### 1. Centralized Configuration

```java
public class NotificationServiceFactory {
    
    private static NotificationService instance;
    
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = createNotificationService();
        }
        return instance;
    }
    
    private static NotificationService createNotificationService() {
        NotificationService service = new NotificationService();
        
        // Register all available channels
        registerEmailChannel(service);
        registerSmsChannel(service);
        registerPushChannel(service);
        
        return service;
    }
    
    private static void registerEmailChannel(NotificationService service) {
        EmailConfig config = EmailConfig.builder()
            .provider(EmailConfig.EmailProvider.SENDGRID)
            .apiKey(System.getenv("SENDGRID_API_KEY"))
            .from(System.getenv("EMAIL_FROM"))
            .build();
        
        if (config.isValid()) {
            service.registerChannel(new EmailNotificationChannel(config));
        }
    }
    
    private static void registerSmsChannel(NotificationService service) {
        SmsConfig config = SmsConfig.builder()
            .provider(SmsConfig.SmsProvider.TWILIO)
            .accountSid(System.getenv("TWILIO_ACCOUNT_SID"))
            .authToken(System.getenv("TWILIO_AUTH_TOKEN"))
            .fromNumber(System.getenv("TWILIO_FROM_NUMBER"))
            .build();
        
        if (config.isValid()) {
            service.registerChannel(new SmsNotificationChannel(config));
        }
    }
    
    private static void registerPushChannel(NotificationService service) {
        PushConfig config = PushConfig.builder()
            .provider(PushConfig.PushProvider.FCM)
            .serverKey(System.getenv("FCM_SERVER_KEY"))
            .build();
        
        if (config.isValid()) {
            service.registerChannel(new PushNotificationChannel(config));
        }
    }
}
```

### 2. Logging and Monitoring

The library uses SLF4J for logging. Configure your logging implementation to capture notification events:

```xml
<!-- logback.xml example -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Notifications library logging -->
    <logger name="com.novacomp.notifications" level="INFO"/>
    
    <root level="WARN">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

### 3. Security Best Practices

```java
// ❌ DON'T: Hardcode credentials
EmailConfig badConfig = EmailConfig.builder()
    .apiKey("sk-1234567890abcdef") // NEVER DO THIS!
    .from("noreply@example.com")
    .build();

// ✅ DO: Use environment variables
EmailConfig goodConfig = EmailConfig.builder()
    .apiKey(System.getenv("SENDGRID_API_KEY"))
    .from(System.getenv("EMAIL_FROM"))
    .build();

// ✅ DO: Use a secure configuration service
EmailConfig secureConfig = EmailConfig.builder()
    .apiKey(configService.getSecret("sendgrid.api.key"))
    .from(configService.get("email.from"))
    .build();
```

---

## Testing

### Unit Testing with Mocks

```java
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {
    
    @Test
    public void testSendEmailNotification() {
        // Create mock channel
        NotificationChannelPort mockChannel = mock(NotificationChannelPort.class);
        when(mockChannel.getChannelType()).thenReturn(NotificationChannel.EMAIL);
        when(mockChannel.send(any(NotificationRequest.class)))
            .thenReturn(NotificationResult.success(NotificationChannel.EMAIL, "test-123"));
        
        // Create service and register mock
        NotificationService service = new NotificationService();
        service.registerChannel(mockChannel);
        
        // Send notification
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        NotificationResult result = service.send(request);
        
        // Verify
        assertTrue(result.isSuccess());
        assertEquals("test-123", result.getMessageId());
        verify(mockChannel, times(1)).send(any(NotificationRequest.class));
    }
}
```

---

## Troubleshooting

### Common Issues

1. **Configuration Error: Channel not registered**
   - Solution: Make sure you call `registerChannel()` before sending notifications

2. **Validation Error: Invalid email address**
   - Solution: Ensure email addresses follow valid format (user@domain.com)

3. **Validation Error: Invalid phone number format**
   - Solution: Use E.164 format for phone numbers (+1234567890)

4. **Sending Error: Failed to send notification**
   - Solution: Check your API keys, network connectivity, and provider status

---

## Additional Resources

- [README.md](README.md) - Main documentation
- [API Reference](https://docs.example.com) - Detailed API documentation
- [Provider Documentation](#provider-docs) - External provider docs
  - [SendGrid API](https://www.twilio.com/docs/sendgrid/api-reference)
  - [Twilio SMS API](https://www.twilio.com/docs/sms)
  - [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)

---

For more examples and use cases, see the `examples/` directory in the repository.
