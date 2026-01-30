package com.novacomp.notifications.infrastructure.adapter.sms;

/**
 * Interface for SMS senders.
 * Strategy Pattern - each provider implements this interface.
 */
public interface SmsSender {
    
    /**
     * Sends an SMS message.
     * 
     * @param message the SMS message to send
     * @return the message ID from the provider
     * @throws Exception if sending fails
     */
    String send(SmsMessage message) throws Exception;
}
