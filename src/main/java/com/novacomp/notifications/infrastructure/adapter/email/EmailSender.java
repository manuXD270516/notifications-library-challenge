package com.novacomp.notifications.infrastructure.adapter.email;

/**
 * Interface for email senders.
 * Strategy Pattern - each provider implements this interface.
 */
public interface EmailSender {
    
    /**
     * Sends an email message.
     * 
     * @param message the email message to send
     * @return the message ID from the provider
     * @throws Exception if sending fails
     */
    String send(EmailMessage message) throws Exception;
}
