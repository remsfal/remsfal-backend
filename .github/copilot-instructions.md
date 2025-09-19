# Copilot Instructions for REMSFAL Backend

## Architecture Overview

This is a **multi-module Maven project** implementing a **microservices architecture** using **Quarkus**. The structure follows a clean separation between core business logic, shared utilities, and service implementations.

### Key Modules
- **`remsfal-core/`**: Core business models (`de.remsfal.core.model`) and API interfaces (`de.remsfal.core.api`) shared across all services
- **`remsfal-common/`**: Shared implementations for authentication, JWT handling, and common utilities  
- **`remsfal-platform/`**: Primary microservice handling user auth, metadata storage, and core CRUD operations (port 8080)
- **`remsfal-ticketing/`**: Document storage and ticketing functionality with Cassandra backend (port 8081)
- **`remsfal-notification/`**: Email and notification services using Kafka messaging
- **`remsfal-test/`**: Shared test utilities and base classes

## Critical Development Patterns

### 1. Service Boundaries & Communication
- Each microservice has its own port, database, and configuration
- **Kafka** is used for async communication between services (topics: `user-notification`, `ocr.documents.*`)
- JWT tokens are shared across services using SmallRye JWT with cookie-based authentication (`remsfal_access_token`)

### 2. Authentication & Security
- **JWT Authentication**: Platform service generates tokens, other services validate using JWKS endpoint
- **Cookie-based sessions**: Access/refresh token pattern using `SessionManager` 
- **Principal injection**: Use `RemsfalPrincipal` (implements `UserModel`) for authenticated user context
- **Google OAuth**: Integrated via `GoogleAuthenticator` for social login

### 3. Data Layer Patterns
- **JPA/Hibernate**: Platform service uses MySQL with Liquibase migrations
- **Cassandra**: Ticketing service for document/chat storage  
- **Entity naming**: All JPA entities end with `Entity` (e.g., `UserEntity`, `ProjectEntity`)
- **Immutable JSON**: Use `ImmutableStyle` annotation with Jackson for all JSON DTOs

### 4. Testing Conventions
- Extend `AbstractServiceTest` for platform service tests (provides transaction management)
- Extend `AbstractKafkaTest` for messaging-related tests
- Use `@QuarkusTest` with appropriate test resources (`KafkaCompanionResource`, `CassandraTestResource`)
- Test data constants in `TestData` class

## Essential Commands

### Development Setup
```bash
# Start infrastructure (MySQL, Kafka, MinIO, etc.)
docker compose up -d

# Build all modules
./mvnw clean install

# Start platform service in dev mode 
./mvnw compile quarkus:dev -pl remsfal-services/remsfal-platform

# Start ticketing service
./mvnw compile quarkus:dev -pl remsfal-services/remsfal-ticketing
```

### Code Quality
```bash
# Run style checks
./mvnw checkstyle:checkstyle

# Run tests for specific module  
./mvnw test -pl remsfal-services/remsfal-platform
```

## Project-Specific Conventions

### Package Structure
- Core interfaces: `de.remsfal.core.api.*Endpoint`
- JSON DTOs: `de.remsfal.core.json.*` (immutable with builder pattern)
- Service implementations: `de.remsfal.service.boundary.*Resource` 
- Business logic: `de.remsfal.service.control.*Controller`
- Data access: `de.remsfal.service.entity.dao.*Repository`

### Configuration Patterns
- Use `application.properties` with profile-specific overrides (`%dev.`, `%test.`)
- JWT configuration keys: `de.remsfal.auth.*`, `mp.jwt.*`
- Database connection pooling and timezone handling for MySQL
- Kafka bootstrap servers on `localhost:39092` for dev

### Key Integration Points
- **JWKS endpoint**: `GET /api/v1/authentication/jwks` (platform service)
- **Health checks**: Standard Quarkus health endpoints
- **OpenAPI**: Generated specs in `target/openapi/` for each service
- **Observability**: OpenTelemetry with Grafana LGTM stack on port 3000

## Common Gotchas
- Services communicate via Kafka topics, not direct HTTP calls
- JWT tokens use cookie authentication, not Authorization headers  
- Platform service must be running for other services to validate JWTs
- Docker Compose uses `include:` directive requiring newer versions
- Use `@Authenticated` on resources requiring login, `RemsfalPrincipal` for user context