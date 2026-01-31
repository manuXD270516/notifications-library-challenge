# Getting Started with Notifications Library

Welcome! This guide will help you quickly understand and use the Notifications Library.

## 🚀 Quick Start (5 minutes)

### 1. Explore the Project

```bash
# Navigate to the project
cd notiicactions-library

# View the structure
ls -la

# Read the main README
cat README.md
```

### 2. Understand the Architecture

The project follows Clean Architecture with three layers:

```
📦 Domain Layer (Core Business Logic)
   └── No external dependencies
   
📦 Application Layer (Use Cases)
   └── Depends only on Domain
   
📦 Infrastructure Layer (External Integrations)
   └── Implements Domain interfaces
```

**Key files to review:**
- `ARCHITECTURE.md` - Detailed architecture documentation
- `PROJECT_SUMMARY.md` - Complete project overview
- `src/main/java/com/novacomp/notifications/domain/` - Core domain models

### 3. Run with Docker (No Java Required!)

```bash
# Build the Docker image
docker-compose build

# Run the container
docker-compose up

# You'll see a welcome message with information about the library
```

### 4. View Examples

```bash
# View the comprehensive examples file
cat examples/NotificationExamples.java

# This file shows how to:
# - Send emails via SendGrid, Mailgun, and SMTP
# - Send SMS via Twilio
# - Send push notifications via FCM
# - Handle errors
# - Use multiple channels
```

---

## 📖 Step-by-Step Learning Path

### Beginner: Understanding the Basics

1. **Read the README.md** (5 min)
   - Understand what the library does
   - See the quick start example
   
2. **Review Domain Models** (10 min)
   ```bash
   # Look at core domain classes
   cat src/main/java/com/novacomp/notifications/domain/model/NotificationRequest.java
   cat src/main/java/com/novacomp/notifications/domain/model/NotificationResult.java
   cat src/main/java/com/novacomp/notifications/domain/model/NotificationChannel.java
   ```

3. **Understand the Main Interface** (5 min)
   ```bash
   cat src/main/java/com/novacomp/notifications/domain/port/NotificationChannelPort.java
   ```

4. **See a Complete Example** (5 min)
   ```bash
   cat examples/NotificationExamples.java
   ```

### Intermediate: Understanding the Design

1. **Study the Architecture** (15 min)
   ```bash
   cat ARCHITECTURE.md
   ```
   Focus on:
   - Clean Architecture layers
   - Design patterns used
   - SOLID principles application

2. **Review Channel Implementations** (15 min)
   ```bash
   # Email channel
   cat src/main/java/com/novacomp/notifications/infrastructure/adapter/email/EmailNotificationChannel.java
   
   # SMS channel
   cat src/main/java/com/novacomp/notifications/infrastructure/adapter/sms/SmsNotificationChannel.java
   
   # Push channel
   cat src/main/java/com/novacomp/notifications/infrastructure/adapter/push/PushNotificationChannel.java
   ```

3. **Understand Configuration** (10 min)
   ```bash
   cat src/main/java/com/novacomp/notifications/infrastructure/config/EmailConfig.java
   cat src/main/java/com/novacomp/notifications/infrastructure/config/SmsConfig.java
   cat src/main/java/com/novacomp/notifications/infrastructure/config/PushConfig.java
   ```

### Advanced: Deep Dive

1. **Study the Application Layer** (15 min)
   ```bash
   cat src/main/java/com/novacomp/notifications/application/service/NotificationService.java
   ```
   Understand:
   - How channels are registered
   - How requests are validated
   - How errors are handled

2. **Review Tests** (20 min)
   ```bash
   # Domain tests
   cat src/test/java/com/novacomp/notifications/domain/model/NotificationRequestTest.java
   
   # Service tests
   cat src/test/java/com/novacomp/notifications/application/service/NotificationServiceTest.java
   
   # Channel tests
   cat src/test/java/com/novacomp/notifications/infrastructure/adapter/email/EmailNotificationChannelTest.java
   ```

3. **Understand Provider Implementations** (15 min)
   ```bash
   # SendGrid implementation
   cat src/main/java/com/novacomp/notifications/infrastructure/adapter/email/SendGridEmailSender.java
   
   # Twilio implementation
   cat src/main/java/com/novacomp/notifications/infrastructure/adapter/sms/TwilioSmsSender.java
   
   # FCM implementation
   cat src/main/java/com/novacomp/notifications/infrastructure/adapter/push/FcmPushSender.java
   ```

---

## 🛠️ How to Use the Library

### Basic Usage Example

```java
// 1. Create the notification service
NotificationService service = new NotificationService();

// 2. Configure a channel (Email with SendGrid)
EmailConfig config = EmailConfig.builder()
    .provider(EmailConfig.EmailProvider.SENDGRID)
    .apiKey(System.getenv("SENDGRID_API_KEY"))
    .from("noreply@example.com")
    .fromName("My App")
    .build();

// 3. Register the channel
service.registerChannel(new EmailNotificationChannel(config));

// 4. Create a notification request
NotificationRequest request = NotificationRequest.builder()
    .channel(NotificationChannel.EMAIL)
    .recipient("user@example.com")
    .subject("Welcome!")
    .message("Thank you for signing up!")
    .build();

// 5. Send the notification
NotificationResult result = service.send(request);

// 6. Check the result
if (result.isSuccess()) {
    System.out.println("✅ Sent! Message ID: " + result.getMessageId());
} else {
    System.out.println("❌ Failed: " + result.getErrorMessage());
}
```

### With Multiple Channels

```java
NotificationService service = new NotificationService();

// Register email channel
EmailConfig emailConfig = EmailConfig.builder()
    .provider(EmailConfig.EmailProvider.SENDGRID)
    .apiKey(System.getenv("SENDGRID_API_KEY"))
    .from("noreply@example.com")
    .build();
service.registerChannel(new EmailNotificationChannel(emailConfig));

// Register SMS channel
SmsConfig smsConfig = SmsConfig.builder()
    .provider(SmsConfig.SmsProvider.TWILIO)
    .accountSid(System.getenv("TWILIO_ACCOUNT_SID"))
    .authToken(System.getenv("TWILIO_AUTH_TOKEN"))
    .fromNumber("+1234567890")
    .build();
service.registerChannel(new SmsNotificationChannel(smsConfig));

// Now you can send to either channel
service.send(emailRequest);  // Sends email
service.send(smsRequest);    // Sends SMS
```

---

## 📚 Documentation Overview

### Essential Reading

1. **README.md** - Main documentation, quick start, API overview
2. **EXAMPLES.md** - Detailed usage examples for all channels
3. **ARCHITECTURE.md** - Architecture, design patterns, SOLID principles
4. **PROJECT_SUMMARY.md** - Complete project overview and statistics

### Reference Documentation

- **DEPLOYMENT.md** - How to deploy in production
- **Javadoc Comments** - Inline documentation in source code
- **Test Files** - See tests for usage examples

### Quick Reference

| I want to... | Go to... |
|-------------|----------|
| Send my first notification | README.md → Quick Start |
| See email examples | EXAMPLES.md → Email section |
| Understand the architecture | ARCHITECTURE.md |
| Add a new channel | ARCHITECTURE.md → Extension Points |
| Deploy to production | DEPLOYMENT.md |
| Run tests | README.md → Testing section |
| Use with Docker | README.md → Docker section |

---

## 🎯 Common Use Cases

### Use Case 1: Send Welcome Email

```java
NotificationService service = createConfiguredService();

NotificationRequest request = NotificationRequest.builder()
    .channel(NotificationChannel.EMAIL)
    .recipient(newUser.getEmail())
    .subject("Welcome to Our Platform!")
    .message("Hi " + newUser.getName() + ", welcome aboard!")
    .priority(NotificationPriority.NORMAL)
    .build();

NotificationResult result = service.send(request);
```

### Use Case 2: Send Verification Code via SMS

```java
String verificationCode = generateCode();

NotificationRequest request = NotificationRequest.builder()
    .channel(NotificationChannel.SMS)
    .recipient(user.getPhoneNumber())  // E.164 format: +1234567890
    .message("Your verification code is: " + verificationCode)
    .priority(NotificationPriority.HIGH)
    .build();

service.send(request);
```

### Use Case 3: Send Critical Alert to Multiple Channels

```java
String alertMessage = "Security alert: Unusual login detected";

// Send email
service.send(NotificationRequest.builder()
    .channel(NotificationChannel.EMAIL)
    .recipient(user.getEmail())
    .subject("Security Alert")
    .message(alertMessage)
    .priority(NotificationPriority.CRITICAL)
    .build());

// Send SMS
service.send(NotificationRequest.builder()
    .channel(NotificationChannel.SMS)
    .recipient(user.getPhoneNumber())
    .message(alertMessage)
    .priority(NotificationPriority.CRITICAL)
    .build());

// Send push notification
service.send(NotificationRequest.builder()
    .channel(NotificationChannel.PUSH)
    .recipient(user.getDeviceToken())
    .subject("Security Alert")
    .message(alertMessage)
    .priority(NotificationPriority.CRITICAL)
    .build());
```

---

## 🔍 Exploring the Code

### Project Structure at a Glance

```
📁 notiicactions-library/
│
├── 📁 src/main/java/com/novacomp/notifications/
│   ├── 📁 domain/              ← Start here! Core business logic
│   │   ├── model/             ← Domain models
│   │   ├── port/              ← Interfaces (contracts)
│   │   └── exception/         ← Domain exceptions
│   │
│   ├── 📁 application/         ← Use cases and orchestration
│   │   └── service/           ← NotificationService (main entry point)
│   │
│   └── 📁 infrastructure/      ← External integrations
│       ├── adapter/           ← Channel implementations
│       │   ├── email/         ← Email channel + providers
│       │   ├── sms/           ← SMS channel + providers
│       │   └── push/          ← Push channel + providers
│       └── config/            ← Configuration classes
│
├── 📁 src/test/java/          ← Unit tests (80%+ coverage)
│
├── 📁 examples/               ← Runnable code examples
│
├── 📄 README.md               ← Start here for overview
├── 📄 EXAMPLES.md             ← Detailed usage examples
├── 📄 ARCHITECTURE.md         ← Design and patterns
├── 📄 PROJECT_SUMMARY.md      ← Complete project overview
├── 📄 DEPLOYMENT.md           ← Production deployment
└── 📄 GETTING_STARTED.md      ← This file!
```

### Navigation Tips

**To understand the flow:**
1. Start with `domain/model/NotificationRequest.java`
2. Look at `domain/port/NotificationChannelPort.java`
3. See `application/service/NotificationService.java`
4. Pick a channel in `infrastructure/adapter/` to see implementation

**To see it in action:**
1. Open `examples/NotificationExamples.java`
2. Find a use case similar to yours
3. Copy and adapt the code

**To understand testing:**
1. Pick any test file in `src/test/java/`
2. See how mocks are used
3. Understand the test structure (Arrange-Act-Assert)

---

## 💡 Pro Tips

### Tip 1: Use Environment Variables

Never hardcode API keys!

```java
// ❌ Bad
EmailConfig config = EmailConfig.builder()
    .apiKey("sk-1234567890")  // Never do this!
    .build();

// ✅ Good
EmailConfig config = EmailConfig.builder()
    .apiKey(System.getenv("SENDGRID_API_KEY"))
    .build();
```

### Tip 2: Reuse NotificationService

Create once, use everywhere:

```java
// ❌ Bad - Creates new service every time
public void sendEmail() {
    NotificationService service = new NotificationService();
    // configure and use...
}

// ✅ Good - Singleton or injected
public class NotificationManager {
    private final NotificationService service;
    
    public NotificationManager() {
        this.service = createConfiguredService();
    }
}
```

### Tip 3: Handle Errors Gracefully

```java
try {
    NotificationResult result = service.send(request);
    
    if (!result.isSuccess()) {
        // Log the error
        log.error("Failed to send notification: {}", result.getErrorMessage());
        
        // Maybe retry or use fallback channel
        tryFallbackChannel(request);
    }
} catch (ValidationException e) {
    // Handle invalid input
    log.warn("Invalid notification request: {}", e.getMessage());
} catch (ConfigurationException e) {
    // Handle configuration issues
    log.error("Notification system misconfigured: {}", e.getMessage());
}
```

### Tip 4: Test with Mocks

The library is designed to be testable:

```java
@Test
void testNotificationFlow() {
    // Create mock channel
    NotificationChannelPort mockChannel = mock(NotificationChannelPort.class);
    when(mockChannel.getChannelType()).thenReturn(NotificationChannel.EMAIL);
    when(mockChannel.send(any())).thenReturn(NotificationResult.success(...));
    
    // Test your code
    NotificationService service = new NotificationService();
    service.registerChannel(mockChannel);
    
    // Verify behavior
    NotificationResult result = service.send(request);
    assertTrue(result.isSuccess());
    verify(mockChannel).send(request);
}
```

---

## ❓ FAQ

### Q: Do I need to install Java?

**A**: No, if you use Docker! Just run `docker-compose up`.

### Q: Can I use this in production?

**A**: The architecture is production-ready, but the provider implementations currently simulate API calls. You'd need to implement actual HTTP calls to provider APIs.

### Q: How do I add a new channel (e.g., Slack)?

**A**: See ARCHITECTURE.md → Extension Points section for detailed instructions.

### Q: Why are API calls simulated?

**A**: This is a code challenge to demonstrate architecture and design skills without requiring actual API keys. The structure is ready for real API integration.

### Q: Can I use this with Spring Boot?

**A**: Absolutely! The library is framework-agnostic. Just inject the `NotificationService` as a bean.

### Q: Where are the tests?

**A**: In `src/test/java/`. Run with `mvn test -s settings.xml`.

### Q: How do I contribute?

**A**: This is a code challenge project, but the architecture supports contributions. See ARCHITECTURE.md for extension points.

---

## 🎓 Learning Resources

### Within This Project

- **Clean Architecture**: See ARCHITECTURE.md
- **Design Patterns**: See ARCHITECTURE.md → Design Patterns
- **SOLID Principles**: See ARCHITECTURE.md → SOLID Principles
- **Testing Best Practices**: See test files in `src/test/java/`

### External Resources

- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [SOLID Principles Explained](https://en.wikipedia.org/wiki/SOLID)
- [Design Patterns](https://refactoring.guru/design-patterns)
- [Java Best Practices](https://www.baeldung.com/java-best-practices)

---

## 🚀 Next Steps

1. ✅ **Read this guide** (you're done!)
2. ⬜ **Explore the code** (start with domain layer)
3. ⬜ **Run the examples** (Docker or with JDK)
4. ⬜ **Read ARCHITECTURE.md** (understand the design)
5. ⬜ **Review the tests** (see how it's tested)
6. ⬜ **Try modifying the code** (add a feature, fix a bug)

---

## 📞 Need Help?

### Documentation

- README.md - Overview and quick start
- EXAMPLES.md - Usage examples
- ARCHITECTURE.md - Design details
- DEPLOYMENT.md - Production guide
- PROJECT_SUMMARY.md - Complete overview

### Source Code

- Domain layer - Core business logic
- Tests - Usage examples and testing patterns
- Examples - Runnable code samples

---

**Happy coding! 🎉**

*This library demonstrates Clean Architecture, SOLID principles, and design patterns in a real-world Java application.*
