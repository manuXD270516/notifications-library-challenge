# Notifications Library

A framework-agnostic and extensible notification library for Java.

## Overview

This library provides a unified abstraction for sending notifications across different channels (Email, Push Notifications, SMS, etc.) without coupling your code to specific providers.

## Features

- ✅ **Unified Interface**: Single API for all notification channels
- ✅ **Framework Agnostic**: Pure Java, no framework dependencies
- ✅ **Extensible Architecture**: Easy to add new channels and providers
- ✅ **SOLID Principles**: Clean architecture with design patterns
- ✅ **Type-Safe Configuration**: Builder pattern for configuration
- ✅ **Error Handling**: Comprehensive error management
- ✅ **Well Tested**: Unit tests with mocks

## Supported Channels

### Mandatory Channels
- **Email** - SMTP support with SendGrid and Mailgun providers
- **Push Notification** - Mobile push notifications
- **SMS** - Text message support

### Optional Channels (Coming Soon)
- Slack
- Discord
- Webhooks

## Technical Requirements

- **Java**: 21 or higher
- **Build Tool**: Maven
- **No Framework Dependencies**: Pure Java library

## Installation

### Option 1: Maven Dependency (Recommended for Production)

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.novacomp</groupId>
    <artifactId>notifications-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Option 2: Build from Source

```bash
# Clone the repository
git clone <repository-url>
cd notiicactions-library

# Build with Maven (requires JDK 21+)
mvn clean install -s settings.xml

# The JAR will be available in target/notifications-library-1.0.0.jar
```

### Option 3: Docker (No JDK Required)

```bash
# Build the Docker image
docker-compose build

# Run the container
docker-compose up

# Or use Docker directly
docker build -t notifications-library:1.0.0 .
docker run -it notifications-library:1.0.0
```

## Quick Start

```java
// Create a notification service
NotificationService notificationService = new NotificationService();

// Configure email provider
EmailConfig emailConfig = EmailConfig.builder()
    .provider(EmailProvider.SENDGRID)
    .apiKey("your-api-key")
    .from("noreply@example.com")
    .build();

EmailNotificationChannel emailChannel = new EmailNotificationChannel(emailConfig);
notificationService.registerChannel(emailChannel);

// Send notification
NotificationRequest request = NotificationRequest.builder()
    .channel(NotificationChannel.EMAIL)
    .recipient("user@example.com")
    .subject("Welcome!")
    .message("Hello from Notifications Library")
    .build();

NotificationResult result = notificationService.send(request);

if (result.isSuccess()) {
    System.out.println("Notification sent successfully!");
} else {
    System.out.println("Error: " + result.getError());
}
```

## Configuration

### Email Configuration

```java
EmailConfig config = EmailConfig.builder()
    .provider(EmailProvider.SENDGRID)
    .apiKey("your-sendgrid-api-key")
    .from("noreply@example.com")
    .fromName("Your App")
    .build();
```

### SMS Configuration

```java
SmsConfig config = SmsConfig.builder()
    .provider(SmsProvider.TWILIO)
    .accountSid("your-account-sid")
    .authToken("your-auth-token")
    .fromNumber("+1234567890")
    .build();
```

### Push Notification Configuration

```java
PushConfig config = PushConfig.builder()
    .provider(PushProvider.FCM)
    .serverKey("your-fcm-server-key")
    .build();
```

## Architecture

This library follows Clean Architecture principles:

```
src/main/java/com/novacomp/notifications/
├── domain/                 # Domain layer (entities, interfaces)
│   ├── model/             # Domain models
│   ├── port/              # Ports (interfaces)
│   └── exception/         # Domain exceptions
├── application/           # Application layer (use cases)
│   └── service/           # Application services
└── infrastructure/        # Infrastructure layer (implementations)
    ├── adapter/           # Adapters for external services
    └── config/            # Configuration classes
```

## Design Patterns

- **Strategy Pattern**: For different notification channels
- **Builder Pattern**: For configuration objects
- **Factory Pattern**: For creating channel instances
- **Adapter Pattern**: For provider integrations

## Local Development & Testing

### Prerequisites

- **JDK 21 or higher** (required for compilation)
- **Maven 3.6+** (for building)
- **Docker** (optional, for containerized deployment)

### Building the Project

```bash
# Clean and build
mvn clean package -s settings.xml

# Build without running tests
mvn clean package -DskipTests -s settings.xml

# Install to local Maven repository
mvn clean install -s settings.xml
```

### Running Tests

The project includes comprehensive unit tests covering all major components.

```bash
# Run all tests
mvn test -s settings.xml

# Run specific test class
mvn test -Dtest=NotificationServiceTest -s settings.xml

# Run with coverage report
mvn clean verify -s settings.xml
```

**Note**: Tests require JDK (not just JRE) for compilation. If you only have JRE installed, you can:
1. Install JDK 21+
2. Use Docker to build and test (see Docker section)
3. Review the test source code in `src/test/java/`

### Running Examples

The `examples/` directory contains usage examples:

```bash
# View the example code
cat examples/NotificationExamples.java

# To run examples (after building):
# 1. Set your API keys as environment variables
export SENDGRID_API_KEY="your-key"
export TWILIO_ACCOUNT_SID="your-sid"
export TWILIO_AUTH_TOKEN="your-token"

# 2. Compile and run (requires JDK)
javac -cp "target/notifications-library-1.0.0.jar:." examples/NotificationExamples.java
java -cp "target/notifications-library-1.0.0.jar:examples" NotificationExamples
```

### Docker Development

Use Docker for a complete development environment without installing Java locally:

```bash
# Build the image
docker-compose build

# Run interactive shell in container
docker-compose run --rm notifications-library /bin/bash

# Inside container, you have access to:
# - Compiled library: /app/notifications-library.jar
# - Source code: /app/src/
# - Examples: /app/examples/

# Build inside container
docker-compose run --rm notifications-library mvn clean package -s settings.xml
```

### IDE Setup

#### IntelliJ IDEA
1. Open project from `pom.xml`
2. Configure Maven settings to use `settings.xml`
3. Ensure JDK 21+ is configured
4. Enable annotation processing for Lombok

#### Eclipse
1. Import as Maven project
2. Configure Maven settings file
3. Install Lombok plugin
4. Set compiler compliance to Java 21

#### VS Code
1. Install Java Extension Pack
2. Open project folder
3. Configure `settings.xml` in Maven extension settings

## Error Handling

The library provides comprehensive error handling:

```java
try {
    NotificationResult result = notificationService.send(request);
    
    if (!result.isSuccess()) {
        // Handle validation errors
        if (result.getError() instanceof ValidationException) {
            // Handle validation
        }
        // Handle sending errors
        else if (result.getError() instanceof SendingException) {
            // Handle sending failure
        }
    }
} catch (NotificationException e) {
    // Handle unexpected errors
    log.error("Notification failed", e);
}
```

## Extending the Library

### Adding a New Provider

```java
public class CustomEmailProvider implements EmailProvider {
    
    @Override
    public SendResult send(EmailMessage message, EmailConfig config) {
        // Implement your provider logic
        return SendResult.success("message-id");
    }
}
```

### Adding a New Channel

```java
public class SlackNotificationChannel implements NotificationChannel {
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        // Implement channel logic
        return NotificationResult.success();
    }
    
    @Override
    public ChannelType getType() {
        return ChannelType.SLACK;
    }
}
```

## Supported Providers

### Email Providers
- **SendGrid** - High-deliverability email service
- **Mailgun** - Developer-friendly email API

### SMS Providers
- **Twilio** - Programmable SMS

### Push Notification Providers
- **Firebase Cloud Messaging (FCM)** - Cross-platform push notifications

## Best Practices

1. **Security**: Never hardcode API keys. Use environment variables or secure configuration.
2. **Error Handling**: Always check `NotificationResult.isSuccess()` before proceeding.
3. **Retry Logic**: Implement retry mechanisms for transient failures.
4. **Rate Limiting**: Be aware of provider rate limits.
5. **Testing**: Use mocks for testing, don't make real API calls in unit tests.

## Contributing

Contributions are welcome! Please read our contributing guidelines.

## License

MIT License - see LICENSE file for details.

## Support

For questions and support, please open an issue on GitHub.
