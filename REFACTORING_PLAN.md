# Plan de Refactorización - Java 21 Avanzado

## 🎯 Objetivo
Elevar el código a nivel senior/arquitecto aplicando características modernas de Java 21 para destacar en la explicación técnica.

---

## 📋 Características de Java Modernas a Implementar

### 1. **Records (Java 14+)** ✅
- Reemplazar clases de datos inmutables con records
- Más conciso y expresivo
- Validación en constructores compactos

### 2. **Sealed Classes/Interfaces (Java 17+)** ✅
- Jerarquías de tipos controladas
- Mejor exhaustividad en pattern matching
- Type safety mejorado

### 3. **Pattern Matching for switch (Java 21)** ✅
- Switch expressions con pattern matching
- Más legible que if-else chains
- Type patterns y guarded patterns

### 4. **Stream API & Collectors Avanzados** ✅
- Procesamiento funcional de colecciones
- Collectors personalizados
- Operaciones paralelas cuando aplique

### 5. **Optional API Avanzado** ✅
- Manejo funcional de valores opcionales
- Evitar nulls explícitos
- Chaining de operaciones

### 6. **Virtual Threads (Java 21)** ✅
- Async notifications con virtual threads
- Mejor escalabilidad
- API moderna de concurrencia

### 7. **Method References & Lambda Optimizations** ✅
- Código más funcional y limpio
- Type inference mejorado
- Composición de funciones

### 8. **Validation con Java Bean Validation** ✅
- Validación declarativa
- Anotaciones custom
- Validators reutilizables

### 9. **CompletableFuture Avanzado** ✅
- Async/await pattern en Java
- Combinación de futures
- Error handling funcional

### 10. **Text Blocks (Java 15+)** ✅
- Strings multilínea
- Mejor legibilidad en templates
- Sin escaping manual

---

## 🔧 Refactorizaciones Específicas

### NotificationRequest → Record con Validation
```java
// ANTES: @Data + @Builder class
// DESPUÉS: record con validation compacta
public record NotificationRequest(
    NotificationChannel channel,
    String recipient,
    String subject,
    String message,
    Map<String, Object> metadata,
    NotificationPriority priority
) {
    // Compact constructor con validación
    public NotificationRequest {
        Objects.requireNonNull(channel, "Channel cannot be null");
        recipient = Optional.ofNullable(recipient)
            .filter(r -> !r.isBlank())
            .orElseThrow(() -> new ValidationException("Recipient is required"));
        // ...
    }
}
```

### NotificationResult → Record
```java
public record NotificationResult(
    boolean success,
    NotificationChannel channel,
    String messageId,
    Optional<String> errorMessage,
    Optional<Throwable> error,
    Instant timestamp
) {
    // Factory methods con Optional
    public static NotificationResult success(...) { }
    public static NotificationResult failure(...) { }
}
```

### Exception Hierarchy → Sealed Classes
```java
public sealed interface NotificationException 
    permits ValidationException, SendingException, ConfigurationException {
    String message();
    Optional<Throwable> cause();
}
```

### NotificationService → Stream API
```java
public class NotificationService {
    
    // Usar ConcurrentHashMap + Stream para thread-safety
    private final Map<NotificationChannel, NotificationChannelPort> channels = 
        new ConcurrentHashMap<>();
    
    public Optional<NotificationChannelPort> findChannel(NotificationChannel type) {
        return Optional.ofNullable(channels.get(type));
    }
    
    public List<NotificationChannel> getRegisteredChannels() {
        return channels.keySet()
            .stream()
            .sorted()
            .toList();
    }
}
```

### EmailNotificationChannel → Pattern Matching Switch
```java
private EmailSender createEmailSender(EmailConfig config) {
    return switch (config.provider()) {
        case SENDGRID -> new SendGridEmailSender(config);
        case MAILGUN -> new MailgunEmailSender(config);
        case SMTP -> new SmtpEmailSender(config);
        // Exhaustive - no default needed con sealed
    };
}
```

### Async NotificationService con Virtual Threads
```java
public class AsyncNotificationService {
    
    private final ExecutorService executor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    public CompletableFuture<NotificationResult> sendAsync(
        NotificationRequest request
    ) {
        return CompletableFuture.supplyAsync(
            () -> notificationService.send(request),
            executor
        );
    }
    
    public CompletableFuture<List<NotificationResult>> sendBatch(
        List<NotificationRequest> requests
    ) {
        return requests.stream()
            .map(this::sendAsync)
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                futures -> CompletableFuture.allOf(
                    futures.toArray(CompletableFuture[]::new)
                ).thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .toList())
            ));
    }
}
```

### Validation API Avanzada
```java
public interface Validator<T> {
    ValidationResult validate(T value);
    
    default Validator<T> and(Validator<T> other) {
        return value -> {
            var result1 = this.validate(value);
            return result1.isValid() 
                ? other.validate(value) 
                : result1;
        };
    }
}

// Uso con composition
var emailValidator = Validators.notBlank()
    .and(Validators.matchesPattern(EMAIL_PATTERN))
    .and(Validators.maxLength(255));
```

### Stream Processing para Multi-Send
```java
public Map<NotificationChannel, NotificationResult> sendToMultipleChannels(
    NotificationRequest request,
    Set<NotificationChannel> channels
) {
    return channels.stream()
        .map(channel -> NotificationRequest.builder()
            .from(request)
            .channel(channel)
            .build())
        .collect(Collectors.toMap(
            NotificationRequest::channel,
            this::send,
            (r1, r2) -> r1,
            LinkedHashMap::new
        ));
}
```

### Result Type con Pattern Matching
```java
public sealed interface Result<T, E> {
    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}
    
    default <U> Result<U, E> map(Function<T, U> mapper) {
        return switch (this) {
            case Success<T, E>(var value) -> new Success<>(mapper.apply(value));
            case Failure<T, E>(var error) -> new Failure<>(error);
        };
    }
}
```

---

## 📦 Nuevas Clases a Crear

1. **`AsyncNotificationService`** - Virtual threads + CompletableFuture
2. **`ValidationResult`** - Result type para validaciones
3. **`NotificationBatch`** - Batch sending con streams
4. **`NotificationMetrics`** - Collectors para métricas
5. **`ChannelSelector`** - Strategy selection con streams
6. **`RetryPolicy`** - Retry con exponential backoff (functional)

---

## 🎨 Antes y Después

### Ejemplo 1: Validación
```java
// ANTES
if (request == null) {
    throw new ValidationException("Request cannot be null");
}
if (request.getRecipient() == null || request.getRecipient().isEmpty()) {
    throw new ValidationException("Recipient is required");
}

// DESPUÉS (functional + Optional)
Optional.ofNullable(request)
    .map(NotificationRequest::recipient)
    .filter(r -> !r.isBlank())
    .orElseThrow(() -> new ValidationException("Recipient is required"));
```

### Ejemplo 2: Factory
```java
// ANTES
private EmailSender createEmailSender(EmailConfig config) {
    if (config.getProvider() == EmailProvider.SENDGRID) {
        return new SendGridEmailSender(config);
    } else if (config.getProvider() == EmailProvider.MAILGUN) {
        return new MailgunEmailSender(config);
    } else {
        throw new ConfigurationException("Unsupported");
    }
}

// DESPUÉS (pattern matching switch)
private EmailSender createEmailSender(EmailConfig config) {
    return switch (config.provider()) {
        case SENDGRID -> new SendGridEmailSender(config);
        case MAILGUN -> new MailgunEmailSender(config);
        case SMTP -> new SmtpEmailSender(config);
    };
}
```

### Ejemplo 3: Async Processing
```java
// ANTES
public void sendToAll(List<NotificationRequest> requests) {
    for (NotificationRequest request : requests) {
        send(request);
    }
}

// DESPUÉS (virtual threads + streams)
public CompletableFuture<List<NotificationResult>> sendToAll(
    List<NotificationRequest> requests
) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        return requests.stream()
            .map(req -> CompletableFuture.supplyAsync(() -> send(req), executor))
            .toList()
            .stream()
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                CompletableFuture::allOf
            ))
            .thenApply(v -> requests.stream()
                .map(this::send)
                .toList());
    }
}
```

---

## ✅ Checklist de Implementación

- [ ] Convertir `NotificationRequest` a record
- [ ] Convertir `NotificationResult` a record
- [ ] Convertir `EmailConfig`, `SmsConfig`, `PushConfig` a records
- [ ] Hacer exceptions sealed interface
- [ ] Refactor switch statements a pattern matching
- [ ] Agregar `AsyncNotificationService` con virtual threads
- [ ] Implementar Stream API en `NotificationService`
- [ ] Crear `ValidationResult` con sealed interface
- [ ] Agregar `NotificationBatch` para batch operations
- [ ] Implementar `Optional` API avanzado
- [ ] Crear `RetryPolicy` funcional
- [ ] Agregar Text Blocks para templates
- [ ] Actualizar tests para cubrir nuevas features
- [ ] Documentar nuevas características Java 21

---

## 🎯 Resultado Esperado

**Código que demuestra:**
1. ✅ Dominio profundo de Java 21
2. ✅ Programación funcional avanzada
3. ✅ Concurrencia moderna (virtual threads)
4. ✅ Type safety con sealed types
5. ✅ API fluida y expresiva
6. ✅ Rendimiento optimizado
7. ✅ Código más conciso y mantenible

**Impacto en entrevista técnica:**
- Demuestra conocimiento de últimas features
- Muestra pensamiento funcional
- Evidencia optimización de performance
- Refleja código production-ready moderno
