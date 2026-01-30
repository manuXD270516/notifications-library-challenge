package com.novacomp.notifications.infrastructure.config;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for email notification channel.
 * Uses Builder pattern for flexible configuration.
 */
@Data
@Builder
public class EmailConfig {
    
    /**
     * The email provider to use (SendGrid, Mailgun, SMTP)
     */
    private EmailProvider provider;
    
    /**
     * API key for the provider (SendGrid, Mailgun)
     */
    private String apiKey;
    
    /**
     * Sender email address
     */
    private String from;
    
    /**
     * Sender name (optional)
     */
    private String fromName;
    
    /**
     * SMTP host (for SMTP provider)
     */
    private String smtpHost;
    
    /**
     * SMTP port (for SMTP provider)
     */
    @Builder.Default
    private int smtpPort = 587;
    
    /**
     * SMTP username (for SMTP provider)
     */
    private String smtpUsername;
    
    /**
     * SMTP password (for SMTP provider)
     */
    private String smtpPassword;
    
    /**
     * Whether to use TLS (for SMTP provider)
     */
    @Builder.Default
    private boolean useTls = true;
    
    /**
     * Validates the configuration
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (provider == null || from == null || from.trim().isEmpty()) {
            return false;
        }
        
        switch (provider) {
            case SENDGRID:
            case MAILGUN:
                return apiKey != null && !apiKey.trim().isEmpty();
            case SMTP:
                return smtpHost != null && !smtpHost.trim().isEmpty()
                    && smtpUsername != null && !smtpUsername.trim().isEmpty()
                    && smtpPassword != null && !smtpPassword.trim().isEmpty();
            default:
                return false;
        }
    }
    
    /**
     * Enum for supported email providers
     */
    public enum EmailProvider {
        /**
         * SendGrid provider
         */
        SENDGRID,
        
        /**
         * Mailgun provider
         */
        MAILGUN,
        
        /**
         * Standard SMTP provider
         */
        SMTP
    }
}
