package com.novacomp.notifications.infrastructure.config;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for Push notification channel.
 * Uses Builder pattern for flexible configuration.
 */
@Data
@Builder
public class PushConfig {
    
    /**
     * The push notification provider to use
     */
    private PushProvider provider;
    
    /**
     * Server key for Firebase Cloud Messaging
     */
    private String serverKey;
    
    /**
     * Project ID for FCM
     */
    private String projectId;
    
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
        if (provider == null) {
            return false;
        }
        
        switch (provider) {
            case FCM:
                return serverKey != null && !serverKey.trim().isEmpty();
            default:
                return apiKey != null && !apiKey.trim().isEmpty();
        }
    }
    
    /**
     * Enum for supported push notification providers
     */
    public enum PushProvider {
        /**
         * Firebase Cloud Messaging
         */
        FCM
    }
}
