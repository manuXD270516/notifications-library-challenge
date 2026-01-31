# Ejemplos Avanzados - Java 21 Features

## 🚀 Características Modernas de Java Demostradas

Este documento muestra ejemplos prácticos de las características avanzadas de Java 21 implementadas en la librería.

---

## 1. Records - Datos Inmutables

### NotificationRequest como Record

```java
// ANTES (con Lombok)
@Data
@Builder
public class NotificationRequest {
    private NotificationChannel channel;
    private String recipient;
    // ...
}

// DESPUÉS (Java 14+ Record)
public record NotificationRequest(
    NotificationChannel channel,
    String recipient,
    String subject,
    String message,
    Map<String, Object> metadata,
    NotificationPriority priority
) {
    // Validación en compact constructor
    public NotificationRequest {
        channel = Objects.requireNonNull(channel, "Channel cannot be null");
        recipient = Optional.ofNullable(recipient)
            .map(String::trim)
            .filter(r -> !r.isEmpty())
            .orElseThrow(() -> new ValidationException("Recipient required"));
        // Inmutabilidad garantizada por el compilador
    }
}
```

### Uso Moderno

```java
// Builder pattern mantenido para compatibilidad
var request = NotificationRequest.builder()
    .channel(NotificationChannel.EMAIL)
    .recipient("user@example.com")
    .message("Hello World")
    .build();

// Acceso directo sin getters
String recipient = request.recipient(); // No más request.getRecipient()

// Métodos con() para actualizaciones inmutables
var updatedRequest = request.withPriority(NotificationPriority.HIGH);

// Optional API integrada
request.subjectOptional().ifPresent(System.out::println);
```

---

## 2. Virtual Threads - Concurrencia Masiva

### AsyncNotificationService con Project Loom

```java
// Crear servicio async con virtual threads (Java 21)
var asyncService = new AsyncNotificationService(notificationService);

// Envío simple async
CompletableFuture<NotificationResult> future = asyncService.sendAsync(request);
future.thenAccept(result -> 
    System.out.println("Sent: " + result.messageId())
);

// Batch con miles de notificaciones (¡virtual threads escalan!)
List<NotificationRequest> thousandsOfRequests = createRequests(10_000);
CompletableFuture<List<NotificationResult>> batchFuture = 
    asyncService.sendBatch(thousandsOfRequests);

// Bloquear hasta completar
List<NotificationResult> results = batchFuture.join();

// Timeout con CompletableFuture API
CompletableFuture<NotificationResult> withTimeout = 
    asyncService.sendWithTimeout(request, Duration.ofSeconds(5));
```

### Ventajas de Virtual Threads

```java
// Comparación: Platform Threads vs Virtual Threads

// ANTES: Platform Threads (limitados, ~miles)
ExecutorService platformThreads = Executors.newFixedThreadPool(100);
// Máximo ~1000-2000 threads concurrentes en práctica

// DESPUÉS: Virtual Threads (millones posibles)
ExecutorService virtualThreads = Executors.newVirtualThreadPerTaskExecutor();
// ¡Millones de threads sin problema de memoria!

// Enviar 100,000 notificaciones concurrentemente
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var futures = requests.stream()
        .map(req -> CompletableFuture.supplyAsync(() -> send(req), executor))
        .toList();
    
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
}
```

---

## 3. Stream API Avanzado

### NotificationService Funcional

```java
var service = new NotificationService();

// 1. Obtener canales registrados (Stream + sorted + toList)
List<NotificationChannel> channels = service.getRegisteredChannels();

// 2. Buscar canal con Optional API
Optional<NotificationChannelPort> channel = 
    service.findChannel(NotificationChannel.EMAIL);

channel.ifPresentOrElse(
    c -> System.out.println("Found: " + c.getChannelType()),
    () -> System.out.println("Not found")
);

// 3. Verificar múltiples canales (Stream + allMatch)
Set<NotificationChannel> required = Set.of(
    NotificationChannel.EMAIL,
    NotificationChannel.SMS
);
boolean allRegistered = service.areAllChannelsRegistered(required);

// 4. Enviar a múltiples canales (Stream + Collectors.toMap)
Map<NotificationChannel, NotificationResult> results = 
    service.sendToMultipleChannels(
        request,
        Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS)
    );

// 5. Estadísticas (Collectors.groupingBy)
Map<String, Long> stats = service.getChannelStatistics();
stats.forEach((channel, count) -> 
    System.out.println(channel + ": " + count)
);
```

### NotificationBatch - Procesamiento Batch Avanzado

```java
var batch = new NotificationBatch(notificationService);

// 1. Envío batch simple
List<NotificationRequest> requests = List.of(req1, req2, req3);
BatchResult result = batch.sendAll(requests);

// Acceso a resultados
System.out.println("Success rate: " + result.successRate() + "%");
System.out.println("Successes: " + result.successCount());
System.out.println("Failures: " + result.failureCount());

// 2. Filtrado funcional (Predicates)
BatchResult highPriorityOnly = batch.sendFiltered(
    requests,
    req -> req.priority() == NotificationPriority.HIGH
);

// 3. Agrupar por canal (Collectors.groupingBy)
Map<NotificationChannel, BatchResult> byChannel = 
    batch.sendGroupedByChannel(requests);

byChannel.forEach((channel, batchResult) -> {
    System.out.println(channel + ": " + batchResult.successRate() + "% success");
});

// 4. Particionar por éxito/fallo (Collectors.partitioningBy)
PartitionedResult partitioned = batch.sendPartitioned(requests);
System.out.println("Successful: " + partitioned.successes().size());
System.out.println("Failed: " + partitioned.failures().size());

// 5. Estadísticas detalladas
BatchStatistics stats = batch.getStatistics(requests);
System.out.println("Total: " + stats.totalRequests());
System.out.println("Unique recipients: " + stats.uniqueRecipients());
System.out.println("Most used: " + stats.mostUsedChannel().orElse(null));
```

---

## 4. Pattern Matching Switch

### Factory Method Moderno

```java
// ANTES: if-else o switch tradicional
private EmailSender createEmailSender(EmailConfig config) {
    if (config.getProvider() == EmailProvider.SENDGRID) {
        return new SendGridEmailSender(config);
    } else if (config.getProvider() == EmailProvider.MAILGUN) {
        return new MailgunEmailSender(config);
    } else {
        throw new ConfigurationException("Unsupported");
    }
}

// DESPUÉS: Switch expression (Java 14+)
private EmailSender createEmailSender(EmailConfig config) {
    return switch (config.provider()) {
        case SENDGRID -> new SendGridEmailSender(config);
        case MAILGUN -> new MailgunEmailSender(config);
        case SMTP -> new SmtpEmailSender(config);
        // No default needed - exhaustive check
    };
}
```

---

## 5. Optional API Avanzado

### Manejo Funcional de Nulls

```java
// NotificationResult con Optional
NotificationResult result = service.send(request);

// 1. ifSuccess / ifFailure - Acciones condicionales
result.ifSuccess(messageId -> 
    log.info("Message sent: {}", messageId)
).ifFailure(error -> 
    log.error("Failed: {}", error)
);

// 2. fold - Transformación funcional
String status = result.fold(
    messageId -> "Sent with ID: " + messageId,
    error -> "Failed: " + error
);

// 3. recoverWith - Valor por defecto
String messageId = result.recoverWith("default-id");

// 4. Optional chaining en Request
request.subjectOptional()
    .filter(s -> s.length() > 10)
    .map(String::toUpperCase)
    .ifPresent(System.out::println);

// 5. Metadata con Optional
request.getMetadata("custom-key")
    .map(Object::toString)
    .ifPresent(value -> log.info("Custom: {}", value));
```

---

## 6. Functional Composition

### Composición de Operaciones

```java
// 1. Envío async con transformación
asyncService.sendAsync(request)
    .thenApply(result -> result.messageId().orElse("N/A"))
    .thenAccept(id -> System.out.println("ID: " + id))
    .exceptionally(ex -> {
        log.error("Error", ex);
        return null;
    });

// 2. Múltiples operaciones encadenadas
var results = requests.stream()
    .filter(req -> req.priority() == NotificationPriority.HIGH)
    .map(req -> req.withChannel(NotificationChannel.EMAIL))
    .map(service::send)
    .filter(NotificationResult::success)
    .map(result -> result.messageId().orElse(""))
    .toList();

// 3. Collectors avanzados
Map<NotificationChannel, Double> successRatesByChannel = requests.stream()
    .map(service::send)
    .collect(Collectors.groupingBy(
        NotificationResult::channel,
        Collectors.collectingAndThen(
            Collectors.partitioningBy(NotificationResult::success),
            partition -> {
                long total = partition.get(true).size() + partition.get(false).size();
                return total == 0 ? 0.0 : (partition.get(true).size() * 100.0) / total;
            }
        )
    ));
```

---

## 7. Batch Processing con Progress

### Monitoreo en Tiempo Real

```java
// Batch con progreso
List<NotificationRequest> largeB atch = createRequests(1000);

CompletableFuture<List<NotificationResult>> future = 
    asyncService.sendBatchWithProgress(largeBatch);

// Output (cada 10 completados):
// Progress: 10/1000 completed
// Progress: 20/1000 completed
// ...
// Progress: 1000/1000 completed

// Esperar resultado
List<NotificationResult> results = future.join();

// Analizar resultados con Stream API
long successCount = results.stream()
    .filter(NotificationResult::success)
    .count();

Map<NotificationChannel, Long> failuresByChannel = results.stream()
    .filter(NotificationResult::isFailure)
    .collect(Collectors.groupingBy(
        NotificationResult::channel,
        Collectors.counting()
    ));
```

---

## 8. Try-with-resources con AutoCloseable

### Gestión Automática de Recursos

```java
// AsyncNotificationService implementa AutoCloseable
try (var asyncService = new AsyncNotificationService(notificationService)) {
    
    // Usar el servicio
    List<CompletableFuture<NotificationResult>> futures = requests.stream()
        .map(asyncService::sendAsync)
        .toList();
    
    // Esperar todos
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    
    // Recolectar resultados
    List<NotificationResult> results = futures.stream()
        .map(CompletableFuture::join)
        .toList();
    
} // Cierre automático del executor de virtual threads
```

---

## 9. Inmutabilidad y Funcional Updates

### Actualizaciones Inmutables con Records

```java
// Record es inmutable - no hay setters
NotificationRequest original = NotificationRequest.builder()
    .channel(NotificationChannel.EMAIL)
    .recipient("user@example.com")
    .message("Hello")
    .build();

// Métodos with*() para crear nuevas instancias
NotificationRequest highPriority = original.withPriority(NotificationPriority.HIGH);
NotificationRequest toSms = original.withChannel(NotificationChannel.SMS);

// Original no ha cambiado (inmutable)
assert original.priority() == NotificationPriority.NORMAL;
assert highPriority.priority() == NotificationPriority.HIGH;
```

---

## 10. Method References y Lambda

### Código Conciso y Expresivo

```java
// Method references
requests.stream()
    .map(service::send)                    // Method reference
    .filter(NotificationResult::success)   // Method reference
    .map(NotificationResult::messageId)    // Method reference
    .flatMap(Optional::stream)             // Method reference
    .forEach(System.out::println);         // Method reference

// Comparado con lambdas explícitas
requests.stream()
    .map(req -> service.send(req))
    .filter(result -> result.success())
    .map(result -> result.messageId())
    .flatMap(opt -> opt.stream())
    .forEach(id -> System.out.println(id));

// Mucho más limpio y legible
```

---

## 11. Ejemplo Completo Real

### Aplicación Práctica de Todas las Features

```java
public class NotificationProcessor {
    
    public static void main(String[] args) {
        // Setup
        var service = new NotificationService();
        service.registerChannel(createEmailChannel());
        service.registerChannel(createSmsChannel());
        
        // Crear requests
        List<NotificationRequest> requests = List.of(
            NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user1@example.com")
                .subject("Welcome")
                .message("Welcome to our platform!")
                .priority(NotificationPriority.HIGH)
                .build(),
            NotificationRequest.builder()
                .channel(NotificationChannel.SMS)
                .recipient("+15551234567")
                .message("Your code: 123456")
                .priority(NotificationPriority.CRITICAL)
                .build()
        );
        
        // Opción 1: Sync con batch processing
        var batch = new NotificationBatch(service);
        BatchResult batchResult = batch.sendAll(requests);
        
        System.out.println("Batch Results:");
        System.out.println("- Total: " + batchResult.totalCount());
        System.out.println("- Success: " + batchResult.successCount());
        System.out.println("- Failed: " + batchResult.failureCount());
        System.out.println("- Success Rate: " + batchResult.successRate() + "%");
        
        // Opción 2: Async con virtual threads
        try (var asyncService = new AsyncNotificationService(service)) {
            
            CompletableFuture<List<NotificationResult>> future = 
                asyncService.sendBatch(requests);
            
            // Procesar resultados de forma funcional
            future.thenAccept(results -> {
                
                // Particionar por éxito/fallo
                Map<Boolean, List<NotificationResult>> partition = results.stream()
                    .collect(Collectors.partitioningBy(NotificationResult::success));
                
                // Procesar éxitos
                partition.get(true).forEach(result -> 
                    result.ifSuccess(msgId -> 
                        log.info("✅ Sent: {} -> {}", result.channel(), msgId)
                    )
                );
                
                // Procesar fallos
                partition.get(false).forEach(result -> 
                    result.ifFailure(error -> 
                        log.error("❌ Failed: {} -> {}", result.channel(), error)
                    )
                );
                
                // Estadísticas por canal
                Map<NotificationChannel, Long> byChannel = results.stream()
                    .collect(Collectors.groupingBy(
                        NotificationResult::channel,
                        Collectors.counting()
                    ));
                
                byChannel.forEach((channel, count) -> 
                    System.out.println("Channel " + channel + ": " + count + " sent")
                );
                
            }).join(); // Esperar completar
        }
    }
}
```

---

## 🎯 Resumen de Features Java 21 Demostradas

| Feature | Ubicación | Beneficio |
|---------|-----------|-----------|
| **Records** | NotificationRequest, NotificationResult | Inmutabilidad, menos boilerplate |
| **Virtual Threads** | AsyncNotificationService | Concurrencia masiva, escalabilidad |
| **Switch Expressions** | Factory methods | Código más limpio, exhaustividad |
| **Stream API** | NotificationBatch, Service | Procesamiento funcional, composición |
| **Optional API** | Result handling | Null-safety, API fluida |
| **Pattern Matching** | Switch statements | Type-safe, expresivo |
| **Method References** | Todo el código | Concisión, legibilidad |
| **CompletableFuture** | AsyncNotificationService | Async/await pattern |
| **Collectors** | Batch statistics | Agregación compleja |
| **AutoCloseable** | AsyncNotificationService | Gestión automática de recursos |

---

## 💡 Por Qué Esto Destaca en Entrevista Técnica

1. **Demuestra conocimiento profundo de Java 21**
   - No solo conoce las features, las aplica apropiadamente
   
2. **Programación funcional moderna**
   - Stream API, Optional, CompletableFuture
   - Composición sobre imperativo
   
3. **Concurrencia avanzada**
   - Virtual threads (cutting-edge)
   - CompletableFuture patterns
   
4. **Type safety y inmutabilidad**
   - Records, sealed interfaces (próximo)
   - Código más seguro y mantenible
   
5. **Performance optimization**
   - Virtual threads escalan a millones
   - Stream API optimizado por compilador
   
6. **Código production-ready**
   - Manejo de errores robusto
   - Logging apropiado
   - Resource management (AutoCloseable)

---

**Este código demuestra nivel Senior/Arquitecto** 🚀
