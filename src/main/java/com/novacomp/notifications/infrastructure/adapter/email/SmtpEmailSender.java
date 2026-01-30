package com.novacomp.notifications.infrastructure.adapter.email;

import com.novacomp.notifications.infrastructure.config.EmailConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * SMTP email sender implementation.
 * This is a mock implementation that simulates sending emails through SMTP.
 * 
 * In a real implementation, this would use JavaMail or similar library.
 */
@Slf4j
public class SmtpEmailSender implements EmailSender {
    
    private final EmailConfig config;
    
    public SmtpEmailSender(EmailConfig config) {
        this.config = config;
    }
    
    @Override
    public String send(EmailMessage message) throws Exception {
        log.debug("Sending email via SMTP to: {}", message.getTo());
        
        // For this challenge, we simulate the SMTP sending
        // In production, you would use JavaMail
        String messageId = simulateSmtpSend(message);
        
        log.info("Email sent via SMTP: messageId={}", messageId);
        return messageId;
    }
    
    /**
     * Simulates sending an email via SMTP.
     * In production, this would use JavaMail.
     * 
     * @param message the email message
     * @return a simulated message ID
     */
    private String simulateSmtpSend(EmailMessage message) {
        log.debug("SMTP Configuration: host={}, port={}, username={}, useTLS={}", 
            config.getSmtpHost(), config.getSmtpPort(), 
            config.getSmtpUsername(), config.isUseTls());
        
        log.debug("Email details: from={}, to={}, subject={}", 
            message.getFrom(), message.getTo(), message.getSubject());
        
        // Simulate successful response
        String messageId = "smtp-" + UUID.randomUUID().toString();
        
        // In production, you would do:
        // Properties props = new Properties();
        // props.put("mail.smtp.auth", "true");
        // props.put("mail.smtp.starttls.enable", String.valueOf(config.isUseTls()));
        // props.put("mail.smtp.host", config.getSmtpHost());
        // props.put("mail.smtp.port", String.valueOf(config.getSmtpPort()));
        // 
        // Session session = Session.getInstance(props, new Authenticator() {
        //     @Override
        //     protected PasswordAuthentication getPasswordAuthentication() {
        //         return new PasswordAuthentication(
        //             config.getSmtpUsername(), 
        //             config.getSmtpPassword()
        //         );
        //     }
        // });
        // 
        // MimeMessage mimeMessage = new MimeMessage(session);
        // mimeMessage.setFrom(new InternetAddress(message.getFrom(), message.getFromName()));
        // mimeMessage.setRecipient(Message.RecipientType.TO, 
        //     new InternetAddress(message.getTo()));
        // mimeMessage.setSubject(message.getSubject());
        // 
        // if (message.isHtml()) {
        //     mimeMessage.setContent(message.getBody(), "text/html; charset=utf-8");
        // } else {
        //     mimeMessage.setText(message.getBody(), "utf-8");
        // }
        // 
        // Transport.send(mimeMessage);
        
        return messageId;
    }
}
