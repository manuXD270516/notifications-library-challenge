package com.novacomp.notifications.infrastructure.adapter.email;

import com.novacomp.notifications.infrastructure.config.EmailConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Mailgun email sender implementation.
 * This is a mock implementation that simulates sending emails through Mailgun API.
 * 
 * In a real implementation, this would use the Mailgun SDK or API.
 */
@Slf4j
public class MailgunEmailSender implements EmailSender {
    
    private static final String MAILGUN_API_URL = "https://api.mailgun.net/v3/";
    
    private final EmailConfig config;
    
    public MailgunEmailSender(EmailConfig config) {
        this.config = config;
    }
    
    @Override
    public String send(EmailMessage message) throws Exception {
        log.debug("Sending email via Mailgun to: {}", message.getTo());
        
        // Build Mailgun API request parameters
        String formData = buildFormData(message);
        
        // For this challenge, we simulate the API call
        // In production, you would make the actual HTTP request
        String messageId = simulateMailgunApiCall(formData);
        
        log.info("Email sent via Mailgun: messageId={}", messageId);
        return messageId;
    }
    
    /**
     * Builds the Mailgun API request form data.
     * 
     * @param message the email message
     * @return the form data string
     */
    private String buildFormData(EmailMessage message) {
        StringBuilder formData = new StringBuilder();
        
        formData.append("from=");
        if (message.getFromName() != null) {
            formData.append(encode(message.getFromName()))
                   .append(" <")
                   .append(encode(message.getFrom()))
                   .append(">");
        } else {
            formData.append(encode(message.getFrom()));
        }
        
        formData.append("&to=").append(encode(message.getTo()));
        formData.append("&subject=").append(encode(message.getSubject()));
        
        if (message.isHtml()) {
            formData.append("&html=").append(encode(message.getBody()));
        } else {
            formData.append("&text=").append(encode(message.getBody()));
        }
        
        return formData.toString();
    }
    
    /**
     * URL encodes a string.
     * 
     * @param value the value to encode
     * @return the encoded value
     */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    
    /**
     * Simulates a Mailgun API call.
     * In production, this would make an actual HTTP request to Mailgun.
     * 
     * @param formData the request form data
     * @return a simulated message ID
     */
    private String simulateMailgunApiCall(String formData) {
        // Log the form data for demonstration
        log.debug("Mailgun API form data: {}", formData);
        
        // Simulate successful response
        String messageId = "mailgun-" + UUID.randomUUID().toString();
        
        // In production, you would do:
        // String domain = extractDomain(config.getFrom());
        // String url = MAILGUN_API_URL + domain + "/messages";
        // 
        // HttpRequest request = HttpRequest.newBuilder()
        //     .uri(URI.create(url))
        //     .header("Authorization", "Basic " + 
        //         Base64.getEncoder().encodeToString(("api:" + config.getApiKey()).getBytes()))
        //     .header("Content-Type", "application/x-www-form-urlencoded")
        //     .POST(HttpRequest.BodyPublishers.ofString(formData))
        //     .build();
        // 
        // HttpResponse<String> response = httpClient.send(request, 
        //     HttpResponse.BodyHandlers.ofString());
        // 
        // Parse response and extract message ID
        
        return messageId;
    }
}
