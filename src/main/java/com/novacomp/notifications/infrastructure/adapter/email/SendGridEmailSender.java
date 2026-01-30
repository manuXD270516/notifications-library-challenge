package com.novacomp.notifications.infrastructure.adapter.email;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.novacomp.notifications.infrastructure.config.EmailConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * SendGrid email sender implementation.
 * This is a mock implementation that simulates sending emails through SendGrid API.
 * 
 * In a real implementation, this would use the SendGrid SDK or API.
 */
@Slf4j
public class SendGridEmailSender implements EmailSender {
    
    private static final String SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send";
    
    private final EmailConfig config;
    private final HttpClient httpClient;
    private final Gson gson;
    
    public SendGridEmailSender(EmailConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
    
    @Override
    public String send(EmailMessage message) throws Exception {
        log.debug("Sending email via SendGrid to: {}", message.getTo());
        
        // Build SendGrid API request payload
        JsonObject payload = buildPayload(message);
        
        // For this challenge, we simulate the API call
        // In production, you would make the actual HTTP request
        String messageId = simulateSendGridApiCall(payload);
        
        log.info("Email sent via SendGrid: messageId={}", messageId);
        return messageId;
    }
    
    /**
     * Builds the SendGrid API request payload.
     * 
     * @param message the email message
     * @return the JSON payload
     */
    private JsonObject buildPayload(EmailMessage message) {
        JsonObject payload = new JsonObject();
        
        // Personalizations
        JsonObject personalization = new JsonObject();
        JsonObject toEmail = new JsonObject();
        toEmail.addProperty("email", message.getTo());
        personalization.add("to", gson.toJsonTree(new JsonObject[]{toEmail}));
        payload.add("personalizations", gson.toJsonTree(new JsonObject[]{personalization}));
        
        // From
        JsonObject from = new JsonObject();
        from.addProperty("email", message.getFrom());
        if (message.getFromName() != null) {
            from.addProperty("name", message.getFromName());
        }
        payload.add("from", from);
        
        // Subject
        payload.addProperty("subject", message.getSubject());
        
        // Content
        JsonObject content = new JsonObject();
        content.addProperty("type", message.isHtml() ? "text/html" : "text/plain");
        content.addProperty("value", message.getBody());
        payload.add("content", gson.toJsonTree(new JsonObject[]{content}));
        
        return payload;
    }
    
    /**
     * Simulates a SendGrid API call.
     * In production, this would make an actual HTTP request to SendGrid.
     * 
     * @param payload the request payload
     * @return a simulated message ID
     */
    private String simulateSendGridApiCall(JsonObject payload) {
        // Log the payload for demonstration
        log.debug("SendGrid API payload: {}", payload);
        
        // Simulate successful response
        String messageId = "sendgrid-" + UUID.randomUUID().toString();
        
        // In production, you would do:
        // HttpRequest request = HttpRequest.newBuilder()
        //     .uri(URI.create(SENDGRID_API_URL))
        //     .header("Authorization", "Bearer " + config.getApiKey())
        //     .header("Content-Type", "application/json")
        //     .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
        //     .build();
        // 
        // HttpResponse<String> response = httpClient.send(request, 
        //     HttpResponse.BodyHandlers.ofString());
        // 
        // Parse response and extract message ID
        
        return messageId;
    }
}
