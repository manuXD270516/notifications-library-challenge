# Deployment Guide

This guide explains how to build, test, and deploy the Notifications Library in different environments.

## Table of Contents

1. [Local Development](#local-development)
2. [Docker Deployment](#docker-deployment)
3. [Production Deployment](#production-deployment)
4. [CI/CD Integration](#cicd-integration)
5. [Configuration Management](#configuration-management)

---

## Local Development

### Requirements

- **JDK 21+** - For compiling the library and running tests
- **Maven 3.6+** - For building and dependency management
- **Git** - For version control

### Setup

```bash
# Clone the repository
git clone <repository-url>
cd notiicactions-library

# Verify Java version
java -version  # Should show Java 21+
javac -version # Should show javac 21+

# Build the project
mvn clean package -s settings.xml

# Run tests
mvn test -s settings.xml

# Run functional demo (Interactive)
mvn clean compile
mvn exec:java -Dexec.mainClass="com.novacomp.notifications.NotificationLibraryDemo"
```

### Quick Functional Demo

To quickly verify the library is working correctly:

**From IntelliJ IDEA**:
1. Open `src/main/java/com/novacomp/notifications/NotificationLibraryDemo.java`
2. Right-click → Run 'NotificationLibraryDemo.main()'
3. Review console output and `logs/notifications-library.log`

**From Command Line**:
```bash
mvn clean compile exec:java -Dexec.mainClass="com.novacomp.notifications.NotificationLibraryDemo"
```

**What it demonstrates**:
- All 3 notification channels (Email, SMS, Push)
- Multi-channel sending with proper recipient validation
- Error handling and validation
- Java 21 features: Records, Virtual Threads, Streams
- Structured logging output

### Development Workflow

```bash
# 1. Create a feature branch
git checkout -b feature/your-feature-name

# 2. Make your changes

# 3. Run tests
mvn clean test -s settings.xml

# 4. Build the project
mvn clean package -s settings.xml

# 5. Commit your changes
git add .
git commit -m "feat: your feature description"

# 6. Push to remote
git push origin feature/your-feature-name
```

---

## Docker Deployment

### Building Docker Image

```bash
# Build the image
docker build -t notifications-library:1.0.0 .

# Or use docker-compose
docker-compose build
```

### Running with Docker

```bash
# Run with docker-compose
docker-compose up

# Run with custom environment variables
docker run -it \
  -e SENDGRID_API_KEY="your-sendgrid-key" \
  -e TWILIO_ACCOUNT_SID="your-twilio-sid" \
  -e TWILIO_AUTH_TOKEN="your-twilio-token" \
  notifications-library:1.0.0

# Run interactive shell
docker run -it notifications-library:1.0.0 /bin/bash
```

### Docker Compose for Development

```yaml
# docker-compose.dev.yml
version: '3.8'

services:
  notifications-dev:
    build:
      context: .
      dockerfile: Dockerfile
    volumes:
      - ./src:/app/src
      - ./examples:/app/examples
      - maven-cache:/root/.m2
    environment:
      - SENDGRID_API_KEY=${SENDGRID_API_KEY}
      - TWILIO_ACCOUNT_SID=${TWILIO_ACCOUNT_SID}
      - TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN}
    command: mvn clean package -s settings.xml

volumes:
  maven-cache:
```

---

## Production Deployment

### Building for Production

```bash
# Build production artifact
mvn clean package -DskipTests -s settings.xml

# The JAR will be in target/notifications-library-1.0.0.jar
```

### Deploying to Maven Repository

#### Deploy to Local Repository

```bash
mvn install -s settings.xml
```

#### Deploy to Remote Repository

```xml
<!-- Add to pom.xml -->
<distributionManagement>
    <repository>
        <id>your-releases</id>
        <url>https://your-nexus-server/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>your-snapshots</id>
        <url>https://your-nexus-server/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

```bash
# Deploy to remote repository
mvn deploy -s settings.xml
```

### Using in Applications

Once deployed, add as a dependency:

```xml
<dependency>
    <groupId>com.novacomp</groupId>
    <artifactId>notifications-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## CI/CD Integration

### GitHub Actions

Create `.github/workflows/build.yml`:

```yaml
name: Build and Test

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven
    
    - name: Build with Maven
      run: mvn clean package -s settings.xml
    
    - name: Run tests
      run: mvn test -s settings.xml
    
    - name: Upload artifacts
      uses: actions/upload-artifact@v3
      with:
        name: notifications-library
        path: target/*.jar
```

### Jenkins Pipeline

Create `Jenkinsfile`:

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven 3.9'
        jdk 'JDK 21'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -s settings.xml'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test -s settings.xml'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh 'mvn deploy -s settings.xml'
            }
        }
    }
    
    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
    }
}
```

### GitLab CI/CD

Create `.gitlab-ci.yml`:

```yaml
image: maven:3.9-eclipse-temurin-21

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"

cache:
  paths:
    - .m2/repository

stages:
  - build
  - test
  - deploy

build:
  stage: build
  script:
    - mvn clean package -DskipTests -s settings.xml
  artifacts:
    paths:
      - target/*.jar
    expire_in: 1 week

test:
  stage: test
  script:
    - mvn test -s settings.xml
  coverage: '/Code coverage: \d+.\d+/'
  artifacts:
    when: always
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml

deploy:
  stage: deploy
  script:
    - mvn deploy -s settings.xml
  only:
    - main
    - tags
```

---

## Configuration Management

### Environment-based Configuration

#### Development

```properties
# application-dev.properties
sendgrid.api.key=test-key-dev
mailgun.api.key=test-key-dev
twilio.account.sid=test-sid-dev
twilio.auth.token=test-token-dev
email.from=noreply-dev@example.com
```

#### Staging

```properties
# application-staging.properties
sendgrid.api.key=${SENDGRID_API_KEY}
mailgun.api.key=${MAILGUN_API_KEY}
twilio.account.sid=${TWILIO_ACCOUNT_SID}
twilio.auth.token=${TWILIO_AUTH_TOKEN}
email.from=noreply-staging@example.com
```

#### Production

```properties
# application-prod.properties
sendgrid.api.key=${SENDGRID_API_KEY}
mailgun.api.key=${MAILGUN_API_KEY}
twilio.account.sid=${TWILIO_ACCOUNT_SID}
twilio.auth.token=${TWILIO_AUTH_TOKEN}
email.from=noreply@example.com
```

### Secrets Management

#### Using Environment Variables

```bash
# Set environment variables
export SENDGRID_API_KEY="sk-..."
export TWILIO_ACCOUNT_SID="AC..."
export TWILIO_AUTH_TOKEN="..."

# Use in application
String apiKey = System.getenv("SENDGRID_API_KEY");
```

#### Using AWS Secrets Manager

```java
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

public class SecretsConfig {
    public static String getSecret(String secretName) {
        SecretsManagerClient client = SecretsManagerClient.create();
        GetSecretValueRequest request = GetSecretValueRequest.builder()
            .secretId(secretName)
            .build();
        return client.getSecretValue(request).secretString();
    }
}

// Usage
String sendGridKey = SecretsConfig.getSecret("prod/sendgrid/api-key");
```

#### Using HashiCorp Vault

```java
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;

public class VaultConfig {
    public static String getSecret(String path, String key) throws Exception {
        VaultConfig config = new VaultConfig()
            .address("https://vault.example.com")
            .token(System.getenv("VAULT_TOKEN"))
            .build();
        
        Vault vault = new Vault(config);
        return vault.logical()
            .read(path)
            .getData()
            .get(key);
    }
}

// Usage
String sendGridKey = VaultConfig.getSecret("secret/notifications", "sendgrid-api-key");
```

---

## Deployment Checklist

### Before Deployment

- [ ] All tests pass
- [ ] Code reviewed and approved
- [ ] Documentation updated
- [ ] Version number updated in `pom.xml`
- [ ] Changelog updated
- [ ] Security scan completed
- [ ] Performance tested
- [ ] API keys configured in target environment
- [ ] Monitoring and alerting configured

### Deployment Steps

1. **Tag the release**
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

2. **Build production artifact**
   ```bash
   mvn clean package -s settings.xml
   ```

3. **Run security scan**
   ```bash
   mvn dependency-check:check -s settings.xml
   ```

4. **Deploy to artifact repository**
   ```bash
   mvn deploy -s settings.xml
   ```

5. **Update dependent applications**
   - Update version in consuming applications
   - Test integration
   - Deploy applications

6. **Verify deployment**
   - Check application logs
   - Test notification sending
   - Verify monitoring dashboards

### Rollback Procedure

If issues are detected after deployment:

1. **Identify the issue**
   - Check logs
   - Review monitoring alerts
   - Test affected functionality

2. **Revert to previous version**
   ```xml
   <!-- In consuming application's pom.xml -->
   <dependency>
       <groupId>com.novacomp</groupId>
       <artifactId>notifications-library</artifactId>
       <version>0.9.0</version> <!-- Previous stable version -->
   </dependency>
   ```

3. **Rebuild and redeploy**
   ```bash
   mvn clean package
   # Deploy application
   ```

4. **Investigate and fix**
   - Identify root cause
   - Fix in development environment
   - Follow deployment process again

---

## Monitoring and Observability

### Logging

The library uses SLF4J for logging. Configure your logging framework:

```xml
<!-- logback.xml -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/notifications.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.novacomp.notifications" level="INFO"/>
    
    <root level="WARN">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### Metrics

Integrate with your monitoring system:

```java
// Example with Micrometer
MeterRegistry registry = new SimpleMeterRegistry();

Counter notificationsSent = Counter.builder("notifications.sent")
    .description("Total notifications sent")
    .tag("channel", "email")
    .register(registry);

notificationsSent.increment();
```

### Health Checks

```java
public class NotificationHealthCheck {
    private final NotificationService service;
    
    public boolean isHealthy() {
        // Check if channels are registered
        return service.getRegisteredChannelsCount() > 0;
    }
}
```

---

## Troubleshooting

### Common Issues

**Issue**: Maven build fails with "No compiler is provided"
- **Solution**: Install JDK (not just JRE) and ensure `JAVA_HOME` points to JDK

**Issue**: Tests fail with connection timeout
- **Solution**: Tests use simulated API calls, should not require network access

**Issue**: Docker build fails
- **Solution**: Ensure Docker daemon is running and you have internet access for Maven downloads

**Issue**: Notifications not sending
- **Solution**: Check API keys are correctly configured and valid

---

## Support

For issues and questions:
- GitHub Issues: <repository-url>/issues
- Email: support@example.com
- Documentation: <repository-url>/wiki
