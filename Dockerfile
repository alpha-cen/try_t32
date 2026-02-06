FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Run as non-root user for security
RUN useradd -r -u 1001 -g root appuser && \
    chown -R appuser:root /app
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
