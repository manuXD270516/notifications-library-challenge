package com.novacomp.notifications.infrastructure.config;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for SMS notification channel.
 * Uses Builder pattern for flexible configuration.
 */
@Data
@Builder
public class SmsConfig {
    
    /**
     * The SMS provider to use
     */
    private SmsProvider provider;
    
    /**
     * Account SID (Twilio)
     */
    private String accountSid;
    
    /**
     * Auth token (Twilio)
     */
    private String authToken;
    
    /**
     * Sender phone number (must include country code, e.g., +1234567890)
     */
    private String fromNumber;
    
    /**
     * API key for other providers
     */
    private String apiKey;
    
    /**
     * Validates the configuration
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (provider == null || fromNumber == null || fromNumber.trim().isEmpty()) {
            return false;
        }
        
        switch (provider) {
            case TWILIO:
                return accountSid != null && !accountSid.trim().isEmpty()
                    && authToken != null && !authToken.trim().isEmpty();
            default:
                return apiKey != null && !apiKey.trim().isEmpty();
        }
    }
    
    /**
     * Enum for supported SMS providers
     */
    public enum SmsProvider {
        /**
         * Twilio SMS provider
         */
        TWILIO
    }
}
