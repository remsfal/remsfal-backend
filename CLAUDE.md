# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

REMSFAL Backend is a multi-module Maven project implementing a microservices architecture using Quarkus for facility management. It works with the remsfal-frontend repository and runs at https://remsfal.online.

## Architecture

### Module Structure

- **remsfal-core**: Core business models (`de.remsfal.core.model`) and API interfaces (`de.remsfal.core.api`) shared across all services
- **remsfal-common**: Shared implementations for authentication, JWT handling, and common utilities
- **remsfal-platform**: Primary microservice handling user auth, metadata storage, and core CRUD operations (port 8080, PostgreSQL database)
- **remsfal-ticketing**: Document storage and ticketing functionality (port 8081, Cassandra database)
- **remsfal-notification**: Email and notification services (port 8082, Kafka-based)
- **remsfal-test**: Shared test utilities and base classes

### Service Communication

- Services communicate asynchronously via **Kafka** (topics: `user-notification`, `ocr.documents.*`)
- Each microservice has its own port, database, and configuration
- JWT tokens are shared across services using SmallRye JWT with cookie-based authentication (`remsfal_access_token`)
- Platform service must be running for other services to validate JWTs via JWKS endpoint at `GET /api/v1/authentication/jwks`

### Authentication & Security

- **JWT Authentication**: Platform service generates tokens, other services validate using JWKS endpoint
- **Cookie-based sessions**: Access/refresh token pattern using `SessionManager`
- **Principal injection**: Use `RemsfalPrincipal` (implements `UserModel`) for authenticated user context
- **Google OAuth**: Integrated via `GoogleAuthenticator` for social login
- Use `@Authenticated` annotation on resources requiring login

## Essential Commands

### Initial Setup

```bash
# Start infrastructure (PostgreSQL, Kafka, Cassandra, MinIO, Grafana)
docker compose up -d
```

**Note**: Requires newer Docker Compose version with `include:` directive support.

### Build and Development

```bash
# Build all modules
./mvnw clean install

# Package the application
./mvnw package

# Start platform service in dev mode (port 8080)
./mvnw compile quarkus:dev -pl remsfal-services/remsfal-platform

# Start ticketing service in dev mode (port 8081)
./mvnw compile quarkus:dev -pl remsfal-services/remsfal-ticketing

# Start notification service in dev mode (port 8082)
./mvnw compile quarkus:dev -pl remsfal-services/remsfal-notification
```

Dev UI available at `http://localhost:{port}/q/dev/` when running in dev mode.

### Testing

```bash
# Run all tests
./mvnw test

# Run tests for specific module
./mvnw test -pl remsfal-services/remsfal-platform

# Run specific test class
./mvnw test -Dtest=UserControllerTest -pl remsfal-services/remsfal-platform
```

### Code Quality

```bash
# Run Checkstyle
./mvnw checkstyle:checkstyle
```

Checkstyle configuration: `src/main/style/checkstyle.xml`

## Code Conventions

### Package Structure

- **Core interfaces**: `de.remsfal.core.api.*Endpoint` - JAX-RS endpoint interfaces with OpenAPI annotations
- **JSON DTOs**: `de.remsfal.core.json.*` - Immutable DTOs using `@ImmutableStyle` and Jackson
- **Service implementations**: `de.remsfal.service.boundary.*Resource` - Implement core endpoint interfaces
- **Business logic**: `de.remsfal.service.control.*Controller` - Business logic layer
- **Data access**: `de.remsfal.service.entity.dao.*Repository` - Data repositories extending `AbstractRepository`
- **JPA Entities**: `de.remsfal.service.entity.dto.*Entity` - All JPA entities end with `Entity` suffix

### Layered Architecture Pattern

When implementing a new feature:

1. Define the API contract in `remsfal-core/src/main/java/de/remsfal/core/api/*Endpoint.java`
2. Create immutable JSON DTOs in `remsfal-core/src/main/java/de/remsfal/core/json/*Json.java` using `@ImmutableStyle`
3. Implement the endpoint in `remsfal-services/remsfal-{service}/src/main/java/de/remsfal/service/boundary/*Resource.java`
4. Add business logic in `de.remsfal.service.control/*Controller.java`
5. Create repository methods in `de.remsfal.service.entity.dao/*Repository.java`
6. Define JPA entities in `de.remsfal.service.entity.dto/*Entity.java`

### Data Layer

- **Platform Service**: PostgreSQL with Liquibase migrations in `remsfal-services/remsfal-platform/src/main/resources/META-INF/liquibase-changelog*.xml`
- **Ticketing Service**: Cassandra for document/chat storage
- All JPA entities extend from base classes and must end with `Entity` suffix
- JSON DTOs use `@Immutable` with `@ImmutableStyle` annotation and Jackson for serialization

### Testing Patterns

- **Platform service tests**: Extend `AbstractServiceTest` for transaction management
- **Kafka tests**: Extend `AbstractKafkaTest` for messaging-related tests
- **Ticketing tests**: Extend `AbstractTicketingTest`
- Use `@QuarkusTest` with appropriate test resources (`KafkaCompanionResource`, `CassandraTestResource`)
- Test data constants should be defined in `TestData` class

## Configuration

### Profile-Specific Configuration

Use `application.properties` with profile-specific overrides:
- `%dev.*` - Development profile
- `%test.*` - Test profile
- Production values (no prefix)

### Key Configuration Areas

- **JWT**: `de.remsfal.auth.*`, `mp.jwt.*`
- **Database**: Connection pooling and timezone handling for PostgreSQL
- **Kafka**: Bootstrap servers on `localhost:39092` for dev
- **Notification Service**: SMTP configuration via `.env` file in `remsfal-services/remsfal-notification/.env` (not committed to Git)

## CI/CD and Quality Gates

- GitHub Actions checks code quality using SonarCloud
- Must pass Quality Gates before PR merge
- Test coverage tracked via JaCoCo
- Checkstyle violations fail the build
- Container images published to GitHub Container Registry

## Common Gotchas

- Services communicate via Kafka topics, not direct HTTP calls
- JWT tokens use cookie authentication, not Authorization headers
- Platform service must be running for other services to validate JWTs
- Docker Compose requires newer versions supporting `include:` directive
- Notification service requires SMTP configuration in `.env` file to send emails

## Observability

- OpenTelemetry integration with Grafana LGTM stack on port 3000
- Kafka UI available on port 8090
- Standard Quarkus health endpoints available
- OpenAPI specs generated in `target/openapi/` for each service
