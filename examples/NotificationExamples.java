package com.novacomp.notifications.examples;

import com.novacomp.notifications.application.service.NotificationService;
import com.novacomp.notifications.domain.model.*;
import com.novacomp.notifications.infrastructure.adapter.email.EmailNotificationChannel;
import com.novacomp.notifications.infrastructure.adapter.sms.SmsNotificationChannel;
import com.novacomp.notifications.infrastructure.adapter.push.PushNotificationChannel;
import com.novacomp.notifications.infrastructure.config.*;

/**
 * Comprehensive examples demonstrating the Notifications Library usage.
 * 
 * This class shows how to:
 * - Configure different notification channels (Email, SMS, Push)
 * - Send notifications through multiple channels
 * - Handle errors and validation
 * - Use different providers (SendGrid, Mailgun, Twilio, FCM)
 */
public class NotificationExamples {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  Notifications Library - Usage Examples");
        System.out.println("=================================================\n");
        
        // Example 1: Email with SendGrid
        sendEmailWithSendGrid();
        
        // Example 2: Email with Mailgun
        sendEmailWithMailgun();
        
        // Example 3: Email with SMTP
        sendEmailWithSmtp();
        
        // Example 4: SMS with Twilio
        sendSmsWithTwilio();
        
        // Example 5: Push Notification with FCM
        sendPushWithFcm();
        
        // Example 6: Multiple Channels
        sendToMultipleChannels();
        
        // Example 7: Error Handling
        demonstrateErrorHandling();
        
        System.out.println("\n=================================================");
        System.out.println("  All examples completed!");
        System.out.println("=================================================");
    }
    
    /**
     * Example 1: Send email using SendGrid
     */
    private static void sendEmailWithSendGrid() {
        System.out.println("\n--- Example 1: Email with SendGrid ---");
        
        try {
            // Create notification service
            NotificationService service = new NotificationService();
            
            // Configure SendGrid
            EmailConfig emailConfig = EmailConfig.builder()
                .provider(EmailConfig.EmailProvider.SENDGRID)
                .apiKey("your-sendgrid-api-key")
                .from("noreply@example.com")
                .fromName("My Application")
                .build();
            
            // Register email channel
            service.registerChannel(new EmailNotificationChannel(emailConfig));
            
            // Send notification
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .subject("Welcome to Our Service!")
                .message("Thank you for signing up. We're excited to have you on board!")
                .priority(NotificationPriority.NORMAL)
                .build();
            
            NotificationResult result = service.send(request);
            
            if (result.isSuccess()) {
                System.out.println("✅ Email sent successfully via SendGrid!");
                System.out.println("   Message ID: " + result.getMessageId());
            } else {
                System.out.println("❌ Failed to send email: " + result.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Example 2: Send email using Mailgun
     */
    private static void sendEmailWithMailgun() {
        System.out.println("\n--- Example 2: Email with Mailgun ---");
        
        try {
            NotificationService service = new NotificationService();
            
            EmailConfig emailConfig = EmailConfig.builder()
                .provider(EmailConfig.EmailProvider.MAILGUN)
                .apiKey("your-mailgun-api-key")
                .from("noreply@yourdomain.com")
                .fromName("Your App")
                .build();
            
            service.registerChannel(new EmailNotificationChannel(emailConfig));
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("customer@example.com")
                .subject("Order Confirmation")
                .message("Your order #12345 has been confirmed and will be shipped soon.")
                .build();
            
            NotificationResult result = service.send(request);
            
            if (result.isSuccess()) {
                System.out.println("✅ Email sent successfully via Mailgun!");
                System.out.println("   Message ID: " + result.getMessageId());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Example 3: Send email using SMTP
     */
    private static void sendEmailWithSmtp() {
        System.out.println("\n--- Example 3: Email with SMTP ---");
        
        try {
            NotificationService service = new NotificationService();
            
            EmailConfig emailConfig = EmailConfig.builder()
                .provider(EmailConfig.EmailProvider.SMTP)
                .from("noreply@company.com")
                .fromName("Company Name")
                .smtpHost("smtp.gmail.com")
                .smtpPort(587)
                .smtpUsername("your-email@gmail.com")
                .smtpPassword("your-app-password")
                .useTls(true)
                .build();
            
            service.registerChannel(new EmailNotificationChannel(emailConfig));
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("employee@company.com")
                .subject("Weekly Report")
                .message("Please find attached your weekly performance report.")
                .build();
            
            NotificationResult result = service.send(request);
            
            if (result.isSuccess()) {
                System.out.println("✅ Email sent successfully via SMTP!");
                System.out.println("   Message ID: " + result.getMessageId());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Example 4: Send SMS using Twilio
     */
    private static void sendSmsWithTwilio() {
        System.out.println("\n--- Example 4: SMS with Twilio ---");
        
        try {
            NotificationService service = new NotificationService();
            
            SmsConfig smsConfig = SmsConfig.builder()
                .provider(SmsConfig.SmsProvider.TWILIO)
                .accountSid("your-twilio-account-sid")
                .authToken("your-twilio-auth-token")
                .fromNumber("+1234567890")
                .build();
            
            service.registerChannel(new SmsNotificationChannel(smsConfig));
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.SMS)
                .recipient("+19876543210")
                .message("Your verification code is: 123456. Valid for 5 minutes.")
                .priority(NotificationPriority.HIGH)
                .build();
            
            NotificationResult result = service.send(request);
            
            if (result.isSuccess()) {
                System.out.println("✅ SMS sent successfully via Twilio!");
                System.out.println("   Message ID: " + result.getMessageId());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Example 5: Send push notification using Firebase Cloud Messaging
     */
    private static void sendPushWithFcm() {
        System.out.println("\n--- Example 5: Push Notification with FCM ---");
        
        try {
            NotificationService service = new NotificationService();
            
            PushConfig pushConfig = PushConfig.builder()
                .provider(PushConfig.PushProvider.FCM)
                .serverKey("your-fcm-server-key")
                .projectId("my-firebase-project")
                .build();
            
            service.registerChannel(new PushNotificationChannel(pushConfig));
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.PUSH)
                .recipient("device-token-from-mobile-app-abc123xyz789")
                .subject("New Message")
                .message("You have received a new message from John Doe")
                .priority(NotificationPriority.HIGH)
                .build();
            
            NotificationResult result = service.send(request);
            
            if (result.isSuccess()) {
                System.out.println("✅ Push notification sent successfully via FCM!");
                System.out.println("   Message ID: " + result.getMessageId());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Example 6: Send notifications to multiple channels
     */
    private static void sendToMultipleChannels() {
        System.out.println("\n--- Example 6: Multiple Channels ---");
        
        try {
            NotificationService service = new NotificationService();
            
            // Register all channels
            EmailConfig emailConfig = EmailConfig.builder()
                .provider(EmailConfig.EmailProvider.SENDGRID)
                .apiKey("sendgrid-key")
                .from("alerts@company.com")
                .build();
            service.registerChannel(new EmailNotificationChannel(emailConfig));
            
            SmsConfig smsConfig = SmsConfig.builder()
                .provider(SmsConfig.SmsProvider.TWILIO)
                .accountSid("twilio-sid")
                .authToken("twilio-token")
                .fromNumber("+1234567890")
                .build();
            service.registerChannel(new SmsNotificationChannel(smsConfig));
            
            PushConfig pushConfig = PushConfig.builder()
                .provider(PushConfig.PushProvider.FCM)
                .serverKey("fcm-key")
                .build();
            service.registerChannel(new PushNotificationChannel(pushConfig));
            
            // Send to all channels
            String message = "Critical Alert: Your account has been accessed from a new location.";
            
            NotificationResult emailResult = service.send(
                NotificationRequest.builder()
                    .channel(NotificationChannel.EMAIL)
                    .recipient("user@example.com")
                    .subject("Security Alert")
                    .message(message)
                    .priority(NotificationPriority.CRITICAL)
                    .build()
            );
            
            NotificationResult smsResult = service.send(
                NotificationRequest.builder()
                    .channel(NotificationChannel.SMS)
                    .recipient("+19876543210")
                    .message(message)
                    .priority(NotificationPriority.CRITICAL)
                    .build()
            );
            
            NotificationResult pushResult = service.send(
                NotificationRequest.builder()
                    .channel(NotificationChannel.PUSH)
                    .recipient("device-token-abc123")
                    .subject("Security Alert")
                    .message(message)
                    .priority(NotificationPriority.CRITICAL)
                    .build()
            );
            
            System.out.println("Email: " + (emailResult.isSuccess() ? "✅ Sent" : "❌ Failed"));
            System.out.println("SMS:   " + (smsResult.isSuccess() ? "✅ Sent" : "❌ Failed"));
            System.out.println("Push:  " + (pushResult.isSuccess() ? "✅ Sent" : "❌ Failed"));
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Example 7: Error handling demonstration
     */
    private static void demonstrateErrorHandling() {
        System.out.println("\n--- Example 7: Error Handling ---");
        
        // Test 1: Invalid email address
        System.out.println("\nTest 1: Invalid email address");
        try {
            NotificationService service = new NotificationService();
            
            EmailConfig emailConfig = EmailConfig.builder()
                .provider(EmailConfig.EmailProvider.SENDGRID)
                .apiKey("test-key")
                .from("noreply@example.com")
                .build();
            service.registerChannel(new EmailNotificationChannel(emailConfig));
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("invalid-email")  // Invalid format
                .subject("Test")
                .message("Test message")
                .build();
            
            service.send(request);
            
        } catch (Exception e) {
            System.out.println("✅ Validation error caught: " + e.getMessage());
        }
        
        // Test 2: Missing subject for email
        System.out.println("\nTest 2: Missing email subject");
        try {
            NotificationService service = new NotificationService();
            
            EmailConfig emailConfig = EmailConfig.builder()
                .provider(EmailConfig.EmailProvider.SENDGRID)
                .apiKey("test-key")
                .from("noreply@example.com")
                .build();
            service.registerChannel(new EmailNotificationChannel(emailConfig));
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                // Missing subject
                .message("Test message")
                .build();
            
            service.send(request);
            
        } catch (Exception e) {
            System.out.println("✅ Validation error caught: " + e.getMessage());
        }
        
        // Test 3: Channel not registered
        System.out.println("\nTest 3: Channel not registered");
        try {
            NotificationService service = new NotificationService();
            // Don't register any channel
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .subject("Test")
                .message("Test message")
                .build();
            
            service.send(request);
            
        } catch (Exception e) {
            System.out.println("✅ Configuration error caught: " + e.getMessage());
        }
        
        // Test 4: Invalid phone number format
        System.out.println("\nTest 4: Invalid phone number format");
        try {
            NotificationService service = new NotificationService();
            
            SmsConfig smsConfig = SmsConfig.builder()
                .provider(SmsConfig.SmsProvider.TWILIO)
                .accountSid("test-sid")
                .authToken("test-token")
                .fromNumber("+1234567890")
                .build();
            service.registerChannel(new SmsNotificationChannel(smsConfig));
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.SMS)
                .recipient("123-456-7890")  // Invalid format (should be E.164)
                .message("Test message")
                .build();
            
            service.send(request);
            
        } catch (Exception e) {
            System.out.println("✅ Validation error caught: " + e.getMessage());
        }
    }
}
