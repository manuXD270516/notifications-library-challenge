# Architecture Documentation

This document describes the architecture and design decisions of the Notifications Library.

## Table of Contents

1. [Overview](#overview)
2. [Clean Architecture](#clean-architecture)
3. [Design Patterns](#design-patterns)
4. [SOLID Principles](#solid-principles)
5. [Package Structure](#package-structure)
6. [Extension Points](#extension-points)

---

## Overview

The Notifications Library is built following **Clean Architecture** principles, ensuring:
- **Framework Independence**: No dependencies on Spring, Quarkus, or any framework
- **Testability**: Easy to test in isolation with mocks
- **Database Independence**: Pure business logic, no database coupling
- **UI Independence**: Can be used from any type of application
- **External Services Independence**: Business rules don't depend on external APIs

### Key Design Goals

1. **Unified Interface**: Single API for all notification channels
2. **Easy Extension**: Adding new channels or providers is straightforward
3. **Type Safety**: Compile-time safety with strong typing
4. **Error Handling**: Comprehensive exception hierarchy
5. **Configuration Flexibility**: Multiple ways to configure providers

---

## Clean Architecture

The library follows the Clean Architecture pattern with three distinct layers:

```
┌─────────────────────────────────────────────────────┐
│                 Domain Layer                         │
│  (Business Logic, Entities, Use Cases, Ports)      │
│                                                      │
│  • NotificationRequest, NotificationResult          │
│  • NotificationChannelPort, NotificationSenderPort  │
│  • Domain Exceptions                                 │
└─────────────────────────────────────────────────────┘
                         ↑
                         │ Dependency Direction
                         │
┌─────────────────────────────────────────────────────┐
│             Application Layer                        │
│         (Application Services)                       │
│                                                      │
│  • NotificationService                               │
└─────────────────────────────────────────────────────┘
                         ↑
                         │
                         │
┌─────────────────────────────────────────────────────┐
│           Infrastructure Layer                       │
│  (External Interfaces, Adapters, Configs)          │
│                                                      │
│  • EmailNotificationChannel                          │
│  • SmsNotificationChannel                            │
│  • PushNotificationChannel                           │
│  • Provider Implementations                          │
│  • Configuration Classes                             │
└─────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### Domain Layer (`domain/`)
- Contains core business logic
- Defines interfaces (ports)
- Contains domain models and value objects
- **No external dependencies**
- Defines exceptions

**Key Components:**
- `NotificationRequest`: Request model
- `NotificationResult`: Response model
- `NotificationChannelPort`: Port for channels
- `NotificationSenderPort`: Port for sending

#### Application Layer (`application/`)
- Implements use cases
- Orchestrates domain objects
- Implements application services
- **Depends only on domain layer**

**Key Components:**
- `NotificationService`: Main orchestration service

#### Infrastructure Layer (`infrastructure/`)
- Implements ports defined in domain
- Contains adapters for external services
- Configuration management
- **Depends on domain and application layers**

**Key Components:**
- Channel adapters (Email, SMS, Push)
- Provider implementations (SendGrid, Twilio, FCM)
- Configuration classes

---

## Design Patterns

### 1. Strategy Pattern

Used for notification channels - each channel is a strategy.

```java
public interface NotificationChannelPort {
    NotificationResult send(NotificationRequest request);
    NotificationChannel getChannelType();
}

// Concrete strategies
public class EmailNotificationChannel implements NotificationChannelPort { }
public class SmsNotificationChannel implements NotificationChannelPort { }
public class PushNotificationChannel implements NotificationChannelPort { }
```

**Benefits:**
- Easy to add new channels
- Swap implementations at runtime
- Each strategy is independently testable

### 2. Factory Method Pattern

Used for creating provider-specific senders.

```java
private EmailSender createEmailSender(EmailConfig config) {
    switch (config.getProvider()) {
        case SENDGRID:
            return new SendGridEmailSender(config);
        case MAILGUN:
            return new MailgunEmailSender(config);
        case SMTP:
            return new SmtpEmailSender(config);
        default:
            throw new ConfigurationException(...);
    }
}
```

**Benefits:**
- Encapsulates object creation
- Easy to add new providers
- Maintains Open/Closed Principle

### 3. Builder Pattern

Used for configuration and request objects.

```java
EmailConfig config = EmailConfig.builder()
    .provider(EmailProvider.SENDGRID)
    .apiKey("key")
    .from("noreply@example.com")
    .build();
```

**Benefits:**
- Immutable objects
- Clear, readable configuration
- Optional parameters handling

### 4. Adapter Pattern

Used to adapt external services to our domain interfaces.

```java
// Domain interface
public interface NotificationChannelPort {
    NotificationResult send(NotificationRequest request);
}

// Adapter
public class EmailNotificationChannel implements NotificationChannelPort {
    private final EmailSender emailSender; // External service
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        // Adapt domain request to external service format
        EmailMessage message = adaptRequest(request);
        String messageId = emailSender.send(message);
        return NotificationResult.success(...);
    }
}
```

**Benefits:**
- Decouples business logic from external APIs
- Easy to swap providers
- Testable with mocks

### 5. Facade Pattern

`NotificationService` acts as a facade, providing a simplified interface.

```java
public class NotificationService {
    private Map<NotificationChannel, NotificationChannelPort> channels;
    
    public NotificationResult send(NotificationRequest request) {
        // Simplified API hiding complexity
        validate(request);
        NotificationChannelPort channel = getChannel(request.getChannel());
        return channel.send(request);
    }
}
```

**Benefits:**
- Simplified API for clients
- Hides complexity of channel management
- Single entry point

### 6. Template Method Pattern

Common validation and sending flow with customizable steps.

```java
public abstract class BaseNotificationChannel implements NotificationChannelPort {
    
    @Override
    public final NotificationResult send(NotificationRequest request) {
        // Template method
        validate(request);
        
        try {
            String messageId = doSend(request);
            return NotificationResult.success(getChannelType(), messageId);
        } catch (Exception e) {
            return NotificationResult.failure(getChannelType(), e);
        }
    }
    
    // Hook methods to be implemented by subclasses
    protected abstract String doSend(NotificationRequest request);
    protected abstract void validate(NotificationRequest request);
}
```

---

## SOLID Principles

### Single Responsibility Principle (SRP)

Each class has one reason to change:

- `NotificationService`: Orchestrates notification sending
- `EmailNotificationChannel`: Handles email notifications
- `SendGridEmailSender`: Sends emails via SendGrid
- `EmailConfig`: Holds email configuration

### Open/Closed Principle (OCP)

Open for extension, closed for modification:

```java
// Adding a new channel doesn't require modifying existing code
public class SlackNotificationChannel implements NotificationChannelPort {
    // New channel implementation
}

// Register it
notificationService.registerChannel(new SlackNotificationChannel(...));
```

### Liskov Substitution Principle (LSP)

All channel implementations can substitute `NotificationChannelPort`:

```java
NotificationChannelPort channel;

// Can use any implementation
channel = new EmailNotificationChannel(...);
channel = new SmsNotificationChannel(...);
channel = new PushNotificationChannel(...);

// All work the same way
channel.send(request);
```

### Interface Segregation Principle (ISP)

Focused interfaces:

```java
// Clients only depend on what they need
public interface NotificationChannelPort {
    NotificationResult send(NotificationRequest request);
    NotificationChannel getChannelType();
    void validate(NotificationRequest request);
}

// Not: one big interface with all methods
```

### Dependency Inversion Principle (DIP)

Depend on abstractions, not concretions:

```java
public class NotificationService {
    // Depends on abstraction
    private Map<NotificationChannel, NotificationChannelPort> channels;
    
    // Not: private Map<NotificationChannel, EmailNotificationChannel>
}
```

---

## Package Structure

```
com.novacomp.notifications/
│
├── domain/                          # Domain Layer
│   ├── model/                       # Domain models
│   │   ├── NotificationChannel.java
│   │   ├── NotificationPriority.java
│   │   ├── NotificationRequest.java
│   │   └── NotificationResult.java
│   │
│   ├── port/                        # Ports (interfaces)
│   │   ├── NotificationChannelPort.java
│   │   └── NotificationSenderPort.java
│   │
│   └── exception/                   # Domain exceptions
│       ├── NotificationException.java
│       ├── ValidationException.java
│       ├── SendingException.java
│       └── ConfigurationException.java
│
├── application/                     # Application Layer
│   └── service/
│       └── NotificationService.java
│
└── infrastructure/                  # Infrastructure Layer
    ├── adapter/                     # Adapters
    │   ├── email/
    │   │   ├── EmailNotificationChannel.java
    │   │   ├── EmailMessage.java
    │   │   ├── EmailSender.java
    │   │   ├── SendGridEmailSender.java
    │   │   ├── MailgunEmailSender.java
    │   │   └── SmtpEmailSender.java
    │   │
    │   ├── sms/
    │   │   ├── SmsNotificationChannel.java
    │   │   ├── SmsMessage.java
    │   │   ├── SmsSender.java
    │   │   └── TwilioSmsSender.java
    │   │
    │   └── push/
    │       ├── PushNotificationChannel.java
    │       ├── PushMessage.java
    │       ├── PushSender.java
    │       └── FcmPushSender.java
    │
    └── config/                      # Configuration
        ├── EmailConfig.java
        ├── SmsConfig.java
        └── PushConfig.java
```

### Package Dependencies

```
domain  ←  application  ←  infrastructure
  ↑                           ↑
  │                           │
  └───────────────────────────┘
       (infrastructure depends on domain)
```

**Rules:**
- Domain has NO dependencies
- Application depends ONLY on domain
- Infrastructure depends on domain and application
- NO circular dependencies

---

## Extension Points

### Adding a New Channel

1. **Define Channel Type** (if not exists)
   ```java
   // In NotificationChannel enum
   public enum NotificationChannel {
       EMAIL, SMS, PUSH, SLACK  // Add new
   }
   ```

2. **Create Configuration**
   ```java
   @Data
   @Builder
   public class SlackConfig {
       private String webhookUrl;
       private String botToken;
       
       public boolean isValid() {
           return webhookUrl != null && !webhookUrl.isEmpty();
       }
   }
   ```

3. **Implement Channel**
   ```java
   public class SlackNotificationChannel implements NotificationChannelPort {
       private final SlackConfig config;
       
       @Override
       public NotificationResult send(NotificationRequest request) {
           // Implementation
       }
       
       @Override
       public NotificationChannel getChannelType() {
           return NotificationChannel.SLACK;
       }
       
       @Override
       public void validate(NotificationRequest request) {
           // Validation logic
       }
   }
   ```

4. **Register and Use**
   ```java
   SlackConfig config = SlackConfig.builder()
       .webhookUrl("https://hooks.slack.com/...")
       .build();
   
   notificationService.registerChannel(
       new SlackNotificationChannel(config)
   );
   ```

### Adding a New Provider

To add a new email provider (e.g., Amazon SES):

1. **Add Provider Enum**
   ```java
   public enum EmailProvider {
       SENDGRID, MAILGUN, SMTP, AMAZON_SES  // Add new
   }
   ```

2. **Implement Sender**
   ```java
   public class AmazonSesEmailSender implements EmailSender {
       @Override
       public String send(EmailMessage message) throws Exception {
           // Amazon SES implementation
       }
   }
   ```

3. **Update Factory**
   ```java
   private EmailSender createEmailSender(EmailConfig config) {
       switch (config.getProvider()) {
           case AMAZON_SES:
               return new AmazonSesEmailSender(config);
           // ... existing cases
       }
   }
   ```

### Custom Validation

Add custom validation logic:

```java
public class CustomEmailChannel extends EmailNotificationChannel {
    
    @Override
    public void validate(NotificationRequest request) {
        super.validate(request);
        
        // Custom validation
        if (request.getMessage().contains("spam")) {
            throw new ValidationException("Suspicious content detected");
        }
    }
}
```

### Async Notifications

Wrap the service for async execution:

```java
public class AsyncNotificationService {
    private final NotificationService service;
    private final ExecutorService executor;
    
    public CompletableFuture<NotificationResult> sendAsync(
        NotificationRequest request
    ) {
        return CompletableFuture.supplyAsync(
            () -> service.send(request),
            executor
        );
    }
}
```

---

## Testing Strategy

### Unit Tests

Test each layer independently:

```java
// Domain tests
@Test
void testNotificationRequestValidation() {
    NotificationRequest request = NotificationRequest.builder()
        .channel(NotificationChannel.EMAIL)
        .recipient("user@example.com")
        .message("Test")
        .build();
    
    assertTrue(request.isValid());
}

// Application tests with mocks
@Test
void testNotificationService() {
    NotificationChannelPort mockChannel = mock(NotificationChannelPort.class);
    when(mockChannel.getChannelType()).thenReturn(NotificationChannel.EMAIL);
    
    NotificationService service = new NotificationService();
    service.registerChannel(mockChannel);
    
    // Test
}

// Infrastructure tests
@Test
void testEmailChannel() {
    EmailConfig config = EmailConfig.builder()
        .provider(EmailProvider.SENDGRID)
        .apiKey("test-key")
        .from("test@example.com")
        .build();
    
    EmailNotificationChannel channel = new EmailNotificationChannel(config);
    // Test
}
```

### Integration Tests

Test complete flows:

```java
@Test
void testCompleteNotificationFlow() {
    // Setup
    NotificationService service = new NotificationService();
    service.registerChannel(new EmailNotificationChannel(...));
    service.registerChannel(new SmsNotificationChannel(...));
    
    // Execute
    NotificationResult emailResult = service.send(emailRequest);
    NotificationResult smsResult = service.send(smsRequest);
    
    // Verify
    assertTrue(emailResult.isSuccess());
    assertTrue(smsResult.isSuccess());
}
```

---

## Performance Considerations

### Resource Management

- Reuse `NotificationService` instance (singleton pattern)
- Channels are registered once at startup
- HTTP clients are reused within senders

### Async Processing

For high-volume scenarios:

```java
ExecutorService executor = Executors.newFixedThreadPool(10);

List<CompletableFuture<NotificationResult>> futures = requests.stream()
    .map(request -> CompletableFuture.supplyAsync(
        () -> service.send(request),
        executor
    ))
    .collect(Collectors.toList());

CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
```

### Caching

Cache frequently used configurations:

```java
public class CachedConfigurationProvider {
    private final Map<String, EmailConfig> configCache = new ConcurrentHashMap<>();
    
    public EmailConfig getConfig(String key) {
        return configCache.computeIfAbsent(key, this::loadConfig);
    }
}
```

---

## Security Considerations

### API Key Management

- **Never hardcode** API keys
- Use environment variables or secrets manager
- Rotate keys regularly

### Input Validation

- Validate all inputs before processing
- Sanitize user-provided content
- Check for injection attacks

### Error Handling

- Don't expose sensitive information in error messages
- Log errors securely
- Use generic error messages for users

---

## Future Enhancements

Potential areas for extension:

1. **Rate Limiting**: Add rate limiting per channel
2. **Retry Logic**: Implement exponential backoff for failures
3. **Message Templating**: Support for message templates
4. **Batch Sending**: Send multiple notifications at once
5. **Delivery Tracking**: Track delivery status
6. **Webhooks**: Receive delivery status updates
7. **Analytics**: Built-in analytics and reporting
8. **Message Queue Integration**: Kafka, RabbitMQ support

---

## References

- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Design Patterns](https://refactoring.guru/design-patterns)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
