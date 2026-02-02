package com.novacomp.notifications;

import com.novacomp.notifications.application.service.NotificationService;
import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;

import com.novacomp.notifications.infrastructure.adapter.email.EmailNotificationChannel;
import com.novacomp.notifications.infrastructure.adapter.sms.SmsNotificationChannel;
import com.novacomp.notifications.infrastructure.adapter.push.PushNotificationChannel;
import com.novacomp.notifications.infrastructure.config.EmailConfig;
import com.novacomp.notifications.infrastructure.config.PushConfig;
import com.novacomp.notifications.infrastructure.config.SmsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class NotificationLibraryDemo {
    private static final Logger log = LoggerFactory.getLogger(NotificationLibraryDemo.class);

    public static void main(String[] args) {
        log.info("=== Iniciando Demo de Notifications Library ===");

        // Crear servicio
        NotificationService service = new NotificationService();

        // Configurar y registrar canales
        setupChannels(service);

        // Escenarios de prueba
        testEmailNotification(service);
        testSmsNotification(service);
        testPushNotification(service);
        testMultiChannelNotification(service);
        testErrorHandling(service);

        log.info("=== Demo Completado ===");
    }

    private static void setupChannels(NotificationService service) {
        log.info("\n--- Configurando Canales ---");

        // Email Channel
        EmailConfig emailConfig = EmailConfig.builder()
                .provider(EmailConfig.EmailProvider.SENDGRID)
                .apiKey("demo-sendgrid-key")
                .from("noreply@example.com")
                .fromName("Demo App")
                .build();
        service.registerChannel(new EmailNotificationChannel(emailConfig));
        log.info("Canal EMAIL registrado");

        // SMS Channel
        SmsConfig smsConfig = SmsConfig.builder()
                .provider(SmsConfig.SmsProvider.TWILIO)
                .accountSid("demo-account-sid")
                .authToken("demo-auth-token")
                .fromNumber("+1234567890")
                .build();
        service.registerChannel(new SmsNotificationChannel(smsConfig));
        log.info("Canal SMS registrado");

        // Push Channel
        PushConfig pushConfig = PushConfig.builder()
                .provider(PushConfig.PushProvider.FCM)
                .serverKey("demo-fcm-server-key")
                .build();
        service.registerChannel(new PushNotificationChannel(pushConfig));
        log.info("Canal PUSH registrado");

        log.info("Canales registrados: {}", service.getRegisteredChannels());
    }

    private static void testEmailNotification(NotificationService service) {
        log.info("\n--- Test 1: Email Notification ---");

        NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .subject("Bienvenido a la plataforma")
                .message("Gracias por registrarte. Este es un mensaje de prueba.")
                .build();

        NotificationResult result = service.send(request);

        result.ifSuccess(messageId ->
                log.info("✅ Email enviado exitosamente. Message ID: {}", messageId)
        ).ifFailure(error ->
                log.error("❌ Error enviando email: {}", error)
        );

        log.info("Resultado: {}", result.toLogString());
    }

    private static void testSmsNotification(NotificationService service) {
        log.info("\n--- Test 2: SMS Notification ---");

        NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.SMS)
                .recipient("+1987654321")
                .message("Tu código de verificación es: 123456")
                .build();

        NotificationResult result = service.send(request);

        result.ifSuccess(messageId ->
                log.info("✅ SMS enviado exitosamente. Message ID: {}", messageId)
        ).ifFailure(error ->
                log.error("❌ Error enviando SMS: {}", error)
        );

        log.info("Resultado: {}", result.toLogString());
    }

    private static void testPushNotification(NotificationService service) {
        log.info("\n--- Test 3: Push Notification ---");

        NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.PUSH)
                .recipient("device-token-1234567890abcdef1234567890abcdef")
                .subject("Nueva actualización")
                .message("Hay una nueva versión disponible")
                .build();

        NotificationResult result = service.send(request);

        result.ifSuccess(messageId ->
                log.info("✅ Push enviado exitosamente. Message ID: {}", messageId)
        ).ifFailure(error ->
                log.error("❌ Error enviando push: {}", error)
        );

        log.info("Resultado: {}", result.toLogString());
    }

    private static void testMultiChannelNotification(NotificationService service) {
        log.info("\n--- Test 4: Multi-Channel Notification ---");

        // Crear requests específicos para cada canal con recipients apropiados
        String baseMessage = "Este mensaje se enviará por múltiples canales";
        String baseSubject = "Notificación importante";
        
        // Email notification
        NotificationRequest emailRequest = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .subject(baseSubject)
                .message(baseMessage)
                .build();
        
        // SMS notification - usar el método withChannelAndRecipient
        NotificationRequest smsRequest = emailRequest
                .withChannelAndRecipient(NotificationChannel.SMS, "+1987654321");
        
        // Push notification - usar el método withChannelAndRecipient
        NotificationRequest pushRequest = emailRequest
                .withChannelAndRecipient(
                    NotificationChannel.PUSH, 
                    "device-token-1234567890abcdef1234567890abcdef"
                );

        log.info("Enviando notificaciones a 3 canales con recipients específicos:");
        
        // Enviar y recolectar resultados
        NotificationResult emailResult = service.send(emailRequest);
        NotificationResult smsResult = service.send(smsRequest);
        NotificationResult pushResult = service.send(pushRequest);
        
        // Mostrar resultados
        log.info("Resultados multi-canal:");
        log.info("  EMAIL -> {} (recipient: {})", 
            emailResult.success() ? "✅ SUCCESS" : "❌ FAILURE",
            emailRequest.recipient());
        log.info("  SMS -> {} (recipient: {})", 
            smsResult.success() ? "✅ SUCCESS" : "❌ FAILURE",
            smsRequest.recipient());
        log.info("  PUSH -> {} (recipient: {})", 
            pushResult.success() ? "✅ SUCCESS" : "❌ FAILURE",
            pushRequest.recipient());
    }

    private static void testErrorHandling(NotificationService service) {
        log.info("\n--- Test 5: Error Handling ---");

        try {
            // Test 1: Recipient vacío (debería fallar en construcción)
            log.info("Test 5.1: Recipient vacío");
            NotificationRequest.builder()
                    .channel(NotificationChannel.EMAIL)
                    .recipient("")
                    .message("Test")
                    .build();
        } catch (Exception e) {
            log.error("❌ Error esperado: {}", e.getMessage());
        }

        try {
            // Test 2: Request null
            log.info("Test 5.2: Request null");
            service.send(null);
        } catch (Exception e) {
            log.error("❌ Error esperado: {}", e.getMessage());
        }

        try {
            // Test 3: Canal no registrado
            log.info("Test 5.3: Canal no registrado");
            service.registerChannel(null);
        } catch (Exception e) {
            log.error("❌ Error esperado: {}", e.getMessage());
        }
    }
}