package com.novacomp.notifications.infrastructure.adapter.sms;

import com.novacomp.notifications.domain.exception.ConfigurationException;
import com.novacomp.notifications.domain.exception.SendingException;
import com.novacomp.notifications.domain.exception.ValidationException;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import com.novacomp.notifications.domain.port.NotificationChannelPort;
import com.novacomp.notifications.infrastructure.config.SmsConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * SMS notification channel implementation.
 * Supports multiple SMS providers: Twilio.
 * 
 * This is an Adapter in the Ports and Adapters (Hexagonal) architecture.
 */
@Slf4j
public class SmsNotificationChannel implements NotificationChannelPort {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"  // E.164 format
    );
    
    private final SmsConfig config;
    private final SmsSender smsSender;
    
    /**
     * Creates a new SMS notification channel with the given configuration.
     * 
     * @param config the SMS configuration
     * @throws ConfigurationException if configuration is invalid
     */
    public SmsNotificationChannel(SmsConfig config) {
        if (config == null) {
            throw new ConfigurationException("SMS configuration cannot be null");
        }
        
        if (!config.isValid()) {
            throw new ConfigurationException("Invalid SMS configuration");
        }
        
        this.config = config;
        this.smsSender = createSmsSender(config);
        
        log.info("SMS notification channel initialized with provider: {}", 
            config.getProvider());
    }
    
    /**
     * Creates the appropriate SMS sender based on the provider.
     * Factory Method pattern.
     * 
     * @param config the SMS configuration
     * @return the SMS sender
     */
    private SmsSender createSmsSender(SmsConfig config) {
        switch (config.getProvider()) {
            case TWILIO:
                return new TwilioSmsSender(config);
            default:
                throw new ConfigurationException(
                    "Unsupported SMS provider: " + config.getProvider()
                );
        }
    }
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        log.debug("Sending SMS to: {}", request.getRecipient());
        
        try {
            // Create SMS message
            SmsMessage message = SmsMessage.builder()
                .from(config.getFromNumber())
                .to(request.getRecipient())
                .body(request.getMessage())
                .build();
            
            // Send SMS
            String messageId = smsSender.send(message);
            
            log.info("SMS sent successfully to: {}, messageId: {}", 
                request.getRecipient(), messageId);
            
            return NotificationResult.success(NotificationChannel.SMS, messageId);
            
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", request.getRecipient(), e);
            throw new SendingException("Failed to send SMS", e);
        }
    }
    
    @Override
    public NotificationChannel getChannelType() {
        return NotificationChannel.SMS;
    }
    
    @Override
    public void validate(NotificationRequest request) {
        if (!isValidPhoneNumber(request.getRecipient())) {
            throw new ValidationException(
                "Invalid phone number format (use E.164 format, e.g., +1234567890): " 
                + request.getRecipient()
            );
        }
        
        if (request.getMessage().length() > 1600) {
            throw new ValidationException(
                "SMS message too long (max 1600 characters): " 
                + request.getMessage().length()
            );
        }
    }
    
    /**
     * Validates a phone number format (E.164).
     * 
     * @param phoneNumber the phone number to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }
}
