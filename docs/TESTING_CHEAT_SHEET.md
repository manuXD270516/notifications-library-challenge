# Quick Testing Reference - Cheat Sheet

## 🚀 Ejecutar Pruebas Rápidamente

### Script Automatizado

```bash
# Linux/Mac
chmod +x run-tests.sh
./run-tests.sh

# Windows
run-tests.bat
```

---

## 📋 Escenarios de Prueba

### ✅ ESCENARIO 1: Compilación Local (Maven)

```bash
# Pre-requisito: JDK 21 instalado
cd /d/works/contractor/novacomp/java-sr-developer/code-challenge/notiicactions-library

# Paso 1: Limpiar y compilar
mvn clean compile -s settings.xml

# Paso 2: Empaquetar
mvn clean package -s settings.xml

# Paso 3: Verificar JAR
ls -lh target/notifications-library-1.0.0.jar
```

**✅ Éxito si:**
- `[INFO] BUILD SUCCESS`
- JAR creado: ~62 KB
- Sin errores de compilación

**❌ Fallo si:**
- `No compiler is provided` → Usar Docker
- `Cannot resolve dependencies` → Verificar `settings.xml`

---

### ✅ ESCENARIO 2: Tests Unitarios

```bash
# Ejecutar todos los tests
mvn test -s settings.xml

# Tests específicos
mvn test -Dtest=NotificationServiceTest -s settings.xml
mvn test -Dtest=EmailNotificationChannelTest -s settings.xml
mvn test -Dtest=NotificationRequestTest -s settings.xml
```

**✅ Éxito si:**
```
Tests run: 50+, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Ver resultados detallados:**
```bash
cat target/surefire-reports/TEST-*.xml
cat target/surefire-reports/*.txt
```

---

### ✅ ESCENARIO 3: Docker Build

```bash
# Build con docker-compose
docker-compose build

# Ver progreso
# - Descarga imágenes base (~1 min)
# - Compila con Maven (~42 seg)
# - Crea imagen (~30 seg)
# Total: ~3-5 min primera vez
```

**✅ Éxito si:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 41.8 s
Successfully built abc123def456
Successfully tagged notifications-library:1.0.0
```

**Verificar imagen:**
```bash
docker images notifications-library
# Debe mostrar: 287 MB
```

---

### ✅ ESCENARIO 4: Docker Run

```bash
# Ejecutar contenedor
docker-compose up

# Ver información del contenedor
docker run --rm notifications-library:1.0.0 sh -c "cat /app/info.sh | bash"
```

**✅ Éxito si:**
- Muestra mensaje de bienvenida
- Exit code: 0
- Sin errores

**Verificar contenido:**
```bash
# Ver JAR
docker run --rm notifications-library:1.0.0 sh -c "ls -lh /app/notifications-library.jar"

# Ver ejemplos
docker run --rm notifications-library:1.0.0 sh -c "ls -la /app/examples"

# Ver código fuente
docker run --rm notifications-library:1.0.0 sh -c "ls -la /app/src"
```

---

### ✅ ESCENARIO 5: Tests en Docker

```bash
# Ejecutar tests dentro de Docker
docker-compose run --rm notifications-library mvn test -s settings.xml

# Ver reportes
docker-compose run --rm notifications-library sh -c "cat target/surefire-reports/*.txt | head -100"
```

---

### ✅ ESCENARIO 6: Java 21 Features - Records

**Test Manual:**

```java
// TestRecords.java
import com.novacomp.notifications.domain.model.*;

public class TestRecords {
    public static void main(String[] args) {
        System.out.println("Testing Records...\n");
        
        // Test 1: Record creation
        NotificationRequest request = NotificationRequest.builder()
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .message("Test")
            .build();
        
        System.out.println("✓ Record created");
        System.out.println("  Channel: " + request.channel());
        System.out.println("  Recipient: " + request.recipient());
        
        // Test 2: Immutability
        NotificationRequest updated = request.withPriority(NotificationPriority.HIGH);
        System.out.println("\n✓ Immutability");
        System.out.println("  Original: " + request.priority());
        System.out.println("  Updated: " + updated.priority());
        
        // Test 3: Optional API
        System.out.println("\n✓ Optional API");
        request.subjectOptional().ifPresentOrElse(
            s -> System.out.println("  Subject: " + s),
            () -> System.out.println("  Subject: empty")
        );
        
        // Test 4: Validation
        System.out.println("\n✓ Validation");
        try {
            NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("")
                .message("Test")
                .build();
            System.out.println("  ❌ Should have thrown exception");
        } catch (Exception e) {
            System.out.println("  ✅ Validation works: " + e.getMessage());
        }
    }
}
```

**Compilar y ejecutar:**
```bash
javac -cp target/notifications-library-1.0.0.jar TestRecords.java
java -cp target/notifications-library-1.0.0.jar:. TestRecords
```

---

### ✅ ESCENARIO 7: Virtual Threads (Async)

**Test Manual:**

```java
// TestVirtualThreads.java
import com.novacomp.notifications.application.service.*;
import com.novacomp.notifications.domain.model.*;
import java.util.*;
import java.util.concurrent.*;

public class TestVirtualThreads {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing Virtual Threads (Java 21)...\n");
        
        // Setup
        NotificationService service = new NotificationService();
        // Register channels...
        
        try (AsyncNotificationService asyncService = new AsyncNotificationService(service)) {
            
            // Test 1: Single async
            System.out.println("Test 1: Single async notification");
            NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .message("Async test")
                .build();
            
            CompletableFuture<NotificationResult> future = asyncService.sendAsync(request);
            NotificationResult result = future.get(5, TimeUnit.SECONDS);
            
            System.out.println("  ✅ Completed: " + result.success());
            
            // Test 2: Batch async
            System.out.println("\nTest 2: Batch async (100 notifications)");
            List<NotificationRequest> requests = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                requests.add(NotificationRequest.builder()
                    .channel(NotificationChannel.EMAIL)
                    .recipient("user" + i + "@example.com")
                    .message("Batch " + i)
                    .build());
            }
            
            long start = System.currentTimeMillis();
            CompletableFuture<List<NotificationResult>> batchFuture = 
                asyncService.sendBatch(requests);
            List<NotificationResult> results = batchFuture.get();
            long duration = System.currentTimeMillis() - start;
            
            System.out.println("  Total: " + results.size());
            System.out.println("  Duration: " + duration + "ms");
            System.out.println("  ✅ Virtual threads work!");
        }
    }
}
```

---

### ✅ ESCENARIO 8: Stream API

**Test Manual:**

```java
// TestStreamAPI.java
import com.novacomp.notifications.application.service.*;
import com.novacomp.notifications.domain.model.*;
import java.util.*;
import java.util.stream.*;

public class TestStreamAPI {
    public static void main(String[] args) {
        System.out.println("Testing Stream API...\n");
        
        NotificationService service = new NotificationService();
        NotificationBatch batch = new NotificationBatch(service);
        
        // Create test requests
        List<NotificationRequest> requests = IntStream.range(0, 10)
            .mapToObj(i -> NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("user" + i + "@example.com")
                .message("Test " + i)
                .priority(i % 2 == 0 ? NotificationPriority.HIGH : NotificationPriority.NORMAL)
                .build())
            .toList(); // Java 16+ toList()
        
        // Test 1: Send all
        System.out.println("Test 1: Batch processing");
        NotificationBatch.BatchResult result = batch.sendAll(requests);
        System.out.println("  Total: " + result.totalCount());
        System.out.println("  Success: " + result.successCount());
        System.out.println("  Rate: " + result.successRate() + "%");
        System.out.println("  ✅ Batch works\n");
        
        // Test 2: Filter
        System.out.println("Test 2: Functional filtering");
        NotificationBatch.BatchResult filtered = batch.sendFiltered(
            requests,
            req -> req.priority() == NotificationPriority.HIGH
        );
        System.out.println("  Filtered: " + filtered.totalCount());
        System.out.println("  ✅ Filtering works\n");
        
        // Test 3: Group by channel
        System.out.println("Test 3: Grouping");
        Map<NotificationChannel, NotificationBatch.BatchResult> grouped = 
            batch.sendGroupedByChannel(requests);
        System.out.println("  Groups: " + grouped.size());
        System.out.println("  ✅ Grouping works\n");
        
        // Test 4: Statistics
        System.out.println("Test 4: Statistics");
        NotificationBatch.BatchStatistics stats = batch.getStatistics(requests);
        System.out.println("  Total: " + stats.totalRequests());
        System.out.println("  Unique: " + stats.uniqueRecipients());
        System.out.println("  ✅ Statistics work\n");
    }
}
```

---

## 🎯 Casos de Prueba Específicos

### Caso 1: Email con SendGrid

```java
EmailConfig config = EmailConfig.builder()
    .provider(EmailConfig.EmailProvider.SENDGRID)
    .apiKey(System.getenv("SENDGRID_API_KEY"))
    .from("noreply@example.com")
    .fromName("Test App")
    .build();

NotificationRequest request = NotificationRequest.builder()
    .channel(NotificationChannel.EMAIL)
    .recipient("test@example.com")
    .subject("Test from SendGrid")
    .message("This is a test email via SendGrid")
    .build();

NotificationResult result = service.send(request);
```

**Validar:**
- ✅ `result.success() == true`
- ✅ `result.messageId().isPresent()`
- ✅ `result.channel() == NotificationChannel.EMAIL`

---

### Caso 2: SMS con Twilio

```java
SmsConfig config = SmsConfig.builder()
    .provider(SmsConfig.SmsProvider.TWILIO)
    .accountSid(System.getenv("TWILIO_ACCOUNT_SID"))
    .authToken(System.getenv("TWILIO_AUTH_TOKEN"))
    .fromNumber("+15551234567")
    .build();

NotificationRequest request = NotificationRequest.builder()
    .channel(NotificationChannel.SMS)
    .recipient("+15559876543")
    .message("Your verification code is: 123456")
    .build();
```

**Validar:**
- ✅ Número en formato E.164
- ✅ Mensaje < 1600 caracteres
- ✅ Result success

---

### Caso 3: Push con FCM

```java
PushConfig config = PushConfig.builder()
    .provider(PushConfig.PushProvider.FCM)
    .serverKey(System.getenv("FCM_SERVER_KEY"))
    .build();

NotificationRequest request = NotificationRequest.builder()
    .channel(NotificationChannel.PUSH)
    .recipient("device-token-abc123")
    .subject("New Message")
    .message("You have a new message")
    .priority(NotificationPriority.HIGH)
    .build();
```

**Validar:**
- ✅ Device token válido
- ✅ Priority mapeado correctamente
- ✅ Result success

---

### Caso 4: Multi-Canal

```java
// Enviar a Email y SMS simultáneamente
Map<NotificationChannel, NotificationResult> results = 
    service.sendToMultipleChannels(
        request,
        Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS)
    );

results.forEach((channel, result) -> 
    System.out.println(channel + ": " + result.success())
);
```

**Validar:**
- ✅ 2 resultados (EMAIL y SMS)
- ✅ Ambos con success
- ✅ Message IDs diferentes

---

### Caso 5: Batch con 1000 Notificaciones

```java
List<NotificationRequest> requests = IntStream.range(0, 1000)
    .mapToObj(i -> NotificationRequest.builder()
        .channel(NotificationChannel.EMAIL)
        .recipient("user" + i + "@example.com")
        .message("Batch test " + i)
        .build())
    .toList();

// Sync batch
NotificationBatch batch = new NotificationBatch(service);
NotificationBatch.BatchResult result = batch.sendAll(requests);

System.out.println("Success rate: " + result.successRate() + "%");
```

**Validar:**
- ✅ 1000 resultados
- ✅ Success rate > 95%
- ✅ Completado en tiempo razonable

---

### Caso 6: Async con Virtual Threads

```java
try (AsyncNotificationService asyncService = new AsyncNotificationService(service)) {
    
    List<NotificationRequest> requests = createLargeList(10_000);
    
    long start = System.currentTimeMillis();
    CompletableFuture<List<NotificationResult>> future = 
        asyncService.sendBatch(requests);
    
    List<NotificationResult> results = future.get();
    long duration = System.currentTimeMillis() - start;
    
    System.out.println("Sent 10,000 notifications in " + duration + "ms");
    System.out.println("Using Virtual Threads (Project Loom)");
}
```

**Validar:**
- ✅ 10,000 completados sin OutOfMemoryError
- ✅ Thread count no explota
- ✅ Significativamente más rápido que sync

---

### Caso 7: Validación de Errores

```java
// Test invalid email
try {
    NotificationRequest invalid = NotificationRequest.builder()
        .channel(NotificationChannel.EMAIL)
        .recipient("invalid-email")
        .subject("Test")
        .message("Test")
        .build();
    
    emailChannel.validate(invalid);
    System.out.println("❌ Should have thrown ValidationException");
} catch (ValidationException e) {
    System.out.println("✅ Validation works: " + e.getMessage());
}

// Test missing subject
try {
    NotificationRequest noSubject = NotificationRequest.builder()
        .channel(NotificationChannel.EMAIL)
        .recipient("test@example.com")
        .message("Test")
        .build();
    
    emailChannel.validate(noSubject);
    System.out.println("❌ Should have thrown ValidationException");
} catch (ValidationException e) {
    System.out.println("✅ Subject validation works");
}

// Test null channel
try {
    NotificationRequest nullChannel = NotificationRequest.builder()
        .channel(null)
        .recipient("test@example.com")
        .message("Test")
        .build();
    
    System.out.println("❌ Should have thrown NullPointerException");
} catch (NullPointerException e) {
    System.out.println("✅ Null check works");
}
```

---

## 🐛 Debug y Troubleshooting

### Ver Logs de Maven

```bash
# Con debug output
mvn test -s settings.xml -X

# Solo errores
mvn test -s settings.xml -e
```

### Ver Logs de Docker

```bash
# Logs del último build
docker-compose logs

# Follow logs en tiempo real
docker-compose logs -f

# Logs de un contenedor específico
docker logs <container-id>
```

### Verificar Configuración

```bash
# Ver settings.xml
cat settings.xml

# Ver pom.xml
cat pom.xml | grep -A 5 "<dependencies>"

# Ver Docker config
cat Dockerfile
cat docker-compose.yml
```

---

## 📊 Checklist de Validación

### Pre-Commit Checklist

- [ ] `mvn clean compile` exitoso
- [ ] `mvn test` pasa todos los tests
- [ ] Sin warnings críticos
- [ ] Documentación actualizada
- [ ] Ejemplos funcionan
- [ ] Docker build exitoso

### Pre-Presentación Checklist

- [ ] Todos los escenarios probados
- [ ] Documentación completa revisada
- [ ] Ejemplos de Java 21 funcionan
- [ ] Virtual threads demostrados
- [ ] Stream API demostrado
- [ ] Records demostrados
- [ ] Git history limpio
- [ ] README actualizado

---

## 🚀 Quick Commands

```bash
# Full test suite (local)
mvn clean verify -s settings.xml

# Full test suite (Docker)
docker-compose build && docker-compose run --rm notifications-library mvn clean verify -s settings.xml

# Quick smoke test
mvn clean package -DskipTests -s settings.xml && ls -lh target/*.jar

# Docker smoke test
docker-compose build && docker-compose up
```

---

## 📈 Métricas Esperadas

| Métrica | Objetivo | Comando |
|---------|----------|---------|
| **Tests passing** | 100% | `mvn test -s settings.xml` |
| **Build time** | <2 min | `time mvn clean package -DskipTests` |
| **Docker build** | <5 min | `time docker-compose build` |
| **JAR size** | <100 KB | `ls -lh target/*.jar` |
| **Image size** | <500 MB | `docker images notifications-library` |
| **Test count** | 50+ | Check surefire reports |
| **Coverage** | >85% | `mvn verify` |

---

## 💡 Tips para la Presentación

1. **Demostrar Records:**
   ```java
   // Mostrar inmutabilidad y validación
   var req = NotificationRequest.builder()...;
   var updated = req.withPriority(HIGH);
   ```

2. **Demostrar Virtual Threads:**
   ```java
   // Mostrar 10,000 notificaciones concurrentes
   asyncService.sendBatch(tenThousandRequests);
   ```

3. **Demostrar Stream API:**
   ```java
   // Mostrar functional processing
   batch.sendFiltered(requests, req -> req.priority() == HIGH);
   ```

4. **Demostrar Optional:**
   ```java
   // Mostrar null-safety
   result.ifSuccess(id -> log.info("Sent: {}", id))
         .ifFailure(err -> log.error("Failed: {}", err));
   ```

---

**Estado**: ✅ **LISTO PARA PROBAR**

**Tiempo estimado**: 15-30 minutos para ejecutar todos los escenarios

**Última actualización**: 31 de Enero, 2026
