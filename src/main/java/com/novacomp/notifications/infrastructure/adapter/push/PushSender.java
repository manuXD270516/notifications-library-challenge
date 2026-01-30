package com.novacomp.notifications.infrastructure.adapter.push;

/**
 * Interface for push notification senders.
 * Strategy Pattern - each provider implements this interface.
 */
public interface PushSender {
    
    /**
     * Sends a push notification.
     * 
     * @param message the push message to send
     * @return the message ID from the provider
     * @throws Exception if sending fails
     */
    String send(PushMessage message) throws Exception;
}
