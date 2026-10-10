FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Run as non-root user for security
RUN addgroup -S tickgroup && adduser -S tickuser -G tickgroup
USER tickuser:tickgroup

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
