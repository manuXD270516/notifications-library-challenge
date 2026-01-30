package com.novacomp.notifications.infrastructure.adapter.email;

import com.novacomp.notifications.domain.exception.ConfigurationException;
import com.novacomp.notifications.domain.exception.SendingException;
import com.novacomp.notifications.domain.exception.ValidationException;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import com.novacomp.notifications.domain.port.NotificationChannelPort;
import com.novacomp.notifications.infrastructure.config.EmailConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Email notification channel implementation.
 * Supports multiple email providers: SendGrid, Mailgun, SMTP.
 * 
 * This is an Adapter in the Ports and Adapters (Hexagonal) architecture.
 */
@Slf4j
public class EmailNotificationChannel implements NotificationChannelPort {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private final EmailConfig config;
    private final EmailSender emailSender;
    
    /**
     * Creates a new email notification channel with the given configuration.
     * 
     * @param config the email configuration
     * @throws ConfigurationException if configuration is invalid
     */
    public EmailNotificationChannel(EmailConfig config) {
        if (config == null) {
            throw new ConfigurationException("Email configuration cannot be null");
        }
        
        if (!config.isValid()) {
            throw new ConfigurationException("Invalid email configuration");
        }
        
        this.config = config;
        this.emailSender = createEmailSender(config);
        
        log.info("Email notification channel initialized with provider: {}", 
            config.getProvider());
    }
    
    /**
     * Creates the appropriate email sender based on the provider.
     * Factory Method pattern.
     * 
     * @param config the email configuration
     * @return the email sender
     */
    private EmailSender createEmailSender(EmailConfig config) {
        switch (config.getProvider()) {
            case SENDGRID:
                return new SendGridEmailSender(config);
            case MAILGUN:
                return new MailgunEmailSender(config);
            case SMTP:
                return new SmtpEmailSender(config);
            default:
                throw new ConfigurationException(
                    "Unsupported email provider: " + config.getProvider()
                );
        }
    }
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        log.debug("Sending email to: {}", request.getRecipient());
        
        try {
            // Create email message
            EmailMessage message = EmailMessage.builder()
                .from(config.getFrom())
                .fromName(config.getFromName())
                .to(request.getRecipient())
                .subject(request.getSubject())
                .body(request.getMessage())
                .build();
            
            // Send email
            String messageId = emailSender.send(message);
            
            log.info("Email sent successfully to: {}, messageId: {}", 
                request.getRecipient(), messageId);
            
            return NotificationResult.success(NotificationChannel.EMAIL, messageId);
            
        } catch (Exception e) {
            log.error("Failed to send email to: {}", request.getRecipient(), e);
            throw new SendingException("Failed to send email", e);
        }
    }
    
    @Override
    public NotificationChannel getChannelType() {
        return NotificationChannel.EMAIL;
    }
    
    @Override
    public void validate(NotificationRequest request) {
        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new ValidationException("Email subject is required");
        }
        
        if (!isValidEmail(request.getRecipient())) {
            throw new ValidationException(
                "Invalid email address: " + request.getRecipient()
            );
        }
    }
    
    /**
     * Validates an email address format.
     * 
     * @param email the email address to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
