package com.novacomp.notifications.infrastructure.adapter.email;

import lombok.Builder;
import lombok.Data;

/**
 * Represents an email message with all necessary information.
 * This is an internal model used by email senders.
 */
@Data
@Builder
public class EmailMessage {
    /**
     * Sender email address
     */
    private String from;
    
    /**
     * Sender name
     */
    private String fromName;
    
    /**
     * Recipient email address
     */
    private String to;
    
    /**
     * Email subject
     */
    private String subject;
    
    /**
     * Email body (can be plain text or HTML)
     */
    private String body;
    
    /**
     * Whether the body is HTML
     */
    @Builder.Default
    private boolean isHtml = false;
}
