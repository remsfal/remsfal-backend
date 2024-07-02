# Build stage
FROM maven:3.8.1-openjdk-17-slim AS build

WORKDIR /app

COPY pom.xml .
COPY remsfal-core/pom.xml remsfal-core/
COPY remsfal-service/pom.xml remsfal-service/
COPY src ./src
COPY remsfal-core/src remsfal-core/src
COPY remsfal-service/src remsfal-service/src

RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/remsfal-service/target/remsfal-service-runner.jar ./remsfal-service-runner.jar

EXPOSE 8080

#enviroment Variables
ENV DB_USERNAME=root
ENV DB_PASSWORD=
ENV DB_URL=jdbc:mysql://mysql:3306/REMSFAL


CMD ["java", "-jar", "remsfal-service-runner.jar"]
