FROM eclipse-temurin:17-jre

WORKDIR /app

COPY remsfal-services/remsfal-platform/target/remsfal-service-runner.jar ./remsfal-service-runner.jar

EXPOSE 8080

ENV DB_USERNAME=remsfaladmin
ENV DB_PASSWORD=remsfalSecret
ENV DB_URL=jdbc:postgresql://postgres:5432/REMSFAL


CMD ["java", "-jar", "remsfal-service-runner.jar"]
