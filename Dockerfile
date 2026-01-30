# Multi-stage Dockerfile for Notifications Library
# This Dockerfile allows running the examples without needing local Java/Maven setup

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY settings.xml .
COPY src ./src
COPY examples ./examples

# Build the library (skip tests as they need full compilation)
RUN mvn clean package -DskipTests -s settings.xml

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built library and examples
COPY --from=build /app/target/*.jar ./notifications-library.jar
COPY --from=build /app/examples ./examples

# Copy source files needed for manual compilation in container
COPY --from=build /app/src ./src

# Set environment variables for examples (override these when running)
ENV SENDGRID_API_KEY="your-sendgrid-api-key"
ENV MAILGUN_API_KEY="your-mailgun-api-key"
ENV TWILIO_ACCOUNT_SID="your-twilio-account-sid"
ENV TWILIO_AUTH_TOKEN="your-twilio-auth-token"
ENV TWILIO_FROM_NUMBER="+1234567890"
ENV FCM_SERVER_KEY="your-fcm-server-key"
ENV EMAIL_FROM="noreply@example.com"
ENV EMAIL_FROM_NAME="Notifications Library"

# Create a simple script to display usage
RUN echo '#!/bin/bash\n\
echo "================================================="\n\
echo "  Notifications Library - Docker Container"\n\
echo "================================================="\n\
echo ""\n\
echo "This container includes the compiled Notifications Library."\n\
echo ""\n\
echo "The library has been successfully built and is available at:"\n\
echo "  /app/notifications-library.jar"\n\
echo ""\n\
echo "Source code is available at:"\n\
echo "  /app/src/"\n\
echo ""\n\
echo "Example code is available at:"\n\
echo "  /app/examples/"\n\
echo ""\n\
echo "To use the library:"\n\
echo "  1. Add it to your classpath"\n\
echo "  2. Import the necessary packages"\n\
echo "  3. Configure your notification channels"\n\
echo ""\n\
echo "Environment variables configured:"\n\
echo "  SENDGRID_API_KEY=$SENDGRID_API_KEY"\n\
echo "  MAILGUN_API_KEY=$MAILGUN_API_KEY"\n\
echo "  TWILIO_ACCOUNT_SID=$TWILIO_ACCOUNT_SID"\n\
echo "  FCM_SERVER_KEY=$FCM_SERVER_KEY"\n\
echo "  EMAIL_FROM=$EMAIL_FROM"\n\
echo ""\n\
echo "================================================="\n\
' > /usr/local/bin/info.sh && chmod +x /usr/local/bin/info.sh

CMD ["/usr/local/bin/info.sh"]
