package com.novacomp.notifications.infrastructure.adapter.sms;

import lombok.Builder;
import lombok.Data;

/**
 * Represents an SMS message with all necessary information.
 * This is an internal model used by SMS senders.
 */
@Data
@Builder
public class SmsMessage {
    /**
     * Sender phone number (E.164 format)
     */
    private String from;
    
    /**
     * Recipient phone number (E.164 format)
     */
    private String to;
    
    /**
     * SMS message body
     */
    private String body;
}
