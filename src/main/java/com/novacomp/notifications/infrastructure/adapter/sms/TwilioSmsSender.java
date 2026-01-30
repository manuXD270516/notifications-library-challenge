package com.novacomp.notifications.infrastructure.adapter.sms;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.novacomp.notifications.infrastructure.config.SmsConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Twilio SMS sender implementation.
 * This is a mock implementation that simulates sending SMS through Twilio API.
 * 
 * In a real implementation, this would use the Twilio SDK or API.
 */
@Slf4j
public class TwilioSmsSender implements SmsSender {
    
    private static final String TWILIO_API_URL = "https://api.twilio.com/2010-04-01/Accounts/";
    
    private final SmsConfig config;
    private final Gson gson;
    
    public TwilioSmsSender(SmsConfig config) {
        this.config = config;
        this.gson = new Gson();
    }
    
    @Override
    public String send(SmsMessage message) throws Exception {
        log.debug("Sending SMS via Twilio to: {}", message.getTo());
        
        // Build Twilio API request parameters
        String formData = buildFormData(message);
        
        // For this challenge, we simulate the API call
        // In production, you would make the actual HTTP request
        String messageId = simulateTwilioApiCall(formData);
        
        log.info("SMS sent via Twilio: messageId={}", messageId);
        return messageId;
    }
    
    /**
     * Builds the Twilio API request form data.
     * 
     * @param message the SMS message
     * @return the form data string
     */
    private String buildFormData(SmsMessage message) {
        StringBuilder formData = new StringBuilder();
        
        formData.append("From=").append(encode(message.getFrom()));
        formData.append("&To=").append(encode(message.getTo()));
        formData.append("&Body=").append(encode(message.getBody()));
        
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
     * Simulates a Twilio API call.
     * In production, this would make an actual HTTP request to Twilio.
     * 
     * @param formData the request form data
     * @return a simulated message ID
     */
    private String simulateTwilioApiCall(String formData) {
        // Log the form data for demonstration
        log.debug("Twilio API form data: {}", formData);
        log.debug("Twilio Account SID: {}", config.getAccountSid());
        
        // Simulate successful response
        String messageId = "twilio-" + UUID.randomUUID().toString();
        
        // In production, you would do:
        // String url = TWILIO_API_URL + config.getAccountSid() + "/Messages.json";
        // 
        // String auth = config.getAccountSid() + ":" + config.getAuthToken();
        // String encodedAuth = Base64.getEncoder()
        //     .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        // 
        // HttpRequest request = HttpRequest.newBuilder()
        //     .uri(URI.create(url))
        //     .header("Authorization", "Basic " + encodedAuth)
        //     .header("Content-Type", "application/x-www-form-urlencoded")
        //     .POST(HttpRequest.BodyPublishers.ofString(formData))
        //     .build();
        // 
        // HttpResponse<String> response = httpClient.send(request, 
        //     HttpResponse.BodyHandlers.ofString());
        // 
        // JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        // messageId = jsonResponse.get("sid").getAsString();
        
        return messageId;
    }
}
