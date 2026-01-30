package com.novacomp.notifications.infrastructure.adapter.push;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.novacomp.notifications.infrastructure.config.PushConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Firebase Cloud Messaging (FCM) push sender implementation.
 * This is a mock implementation that simulates sending push notifications through FCM API.
 * 
 * In a real implementation, this would use the Firebase Admin SDK or FCM HTTP v1 API.
 */
@Slf4j
public class FcmPushSender implements PushSender {
    
    private static final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/";
    
    private final PushConfig config;
    private final Gson gson;
    
    public FcmPushSender(PushConfig config) {
        this.config = config;
        this.gson = new Gson();
    }
    
    @Override
    public String send(PushMessage message) throws Exception {
        log.debug("Sending push notification via FCM to device: {}", 
            message.getDeviceToken());
        
        // Build FCM API request payload
        JsonObject payload = buildPayload(message);
        
        // For this challenge, we simulate the API call
        // In production, you would make the actual HTTP request
        String messageId = simulateFcmApiCall(payload);
        
        log.info("Push notification sent via FCM: messageId={}", messageId);
        return messageId;
    }
    
    /**
     * Builds the FCM API request payload.
     * 
     * @param message the push message
     * @return the JSON payload
     */
    private JsonObject buildPayload(PushMessage message) {
        JsonObject payload = new JsonObject();
        
        // Message object
        JsonObject messageObj = new JsonObject();
        
        // Token
        messageObj.addProperty("token", message.getDeviceToken());
        
        // Notification
        JsonObject notification = new JsonObject();
        notification.addProperty("title", message.getTitle());
        notification.addProperty("body", message.getBody());
        messageObj.add("notification", notification);
        
        // Android config (optional)
        JsonObject androidConfig = new JsonObject();
        androidConfig.addProperty("priority", 
            message.getPriority() == PushMessage.Priority.HIGH ? "high" : "normal");
        messageObj.add("android", androidConfig);
        
        // APNS config (optional)
        JsonObject apnsConfig = new JsonObject();
        JsonObject apnsHeaders = new JsonObject();
        apnsHeaders.addProperty("apns-priority", 
            message.getPriority() == PushMessage.Priority.HIGH ? "10" : "5");
        apnsConfig.add("headers", apnsHeaders);
        messageObj.add("apns", apnsConfig);
        
        payload.add("message", messageObj);
        
        return payload;
    }
    
    /**
     * Simulates an FCM API call.
     * In production, this would make an actual HTTP request to FCM.
     * 
     * @param payload the request payload
     * @return a simulated message ID
     */
    private String simulateFcmApiCall(JsonObject payload) {
        // Log the payload for demonstration
        log.debug("FCM API payload: {}", payload);
        
        // Simulate successful response
        String messageId = "fcm-" + UUID.randomUUID().toString();
        
        // In production, you would do:
        // String projectId = config.getProjectId();
        // String url = FCM_API_URL + projectId + "/messages:send";
        // 
        // HttpRequest request = HttpRequest.newBuilder()
        //     .uri(URI.create(url))
        //     .header("Authorization", "Bearer " + getAccessToken())
        //     .header("Content-Type", "application/json")
        //     .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
        //     .build();
        // 
        // HttpResponse<String> response = httpClient.send(request, 
        //     HttpResponse.BodyHandlers.ofString());
        // 
        // JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        // messageId = jsonResponse.get("name").getAsString();
        
        return messageId;
    }
}
