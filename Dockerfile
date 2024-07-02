FROM eclipse-temurin:latest

WORKDIR /app

COPY target/remsfal-service-runner.jar ./remsfal-service-runner.jar

EXPOSE 8080

ENV DB_USERNAME=root
ENV DB_PASSWORD=
ENV DB_URL=jdbc:mysql://mysql:3306/REMSFAL


CMD ["java", "-jar", "remsfal-service-runner.jar"]
