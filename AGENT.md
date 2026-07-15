# AGENT.md — Central AI Project Specification

> This is the canonical source of truth for AI-assisted development on the REMSFAL Backend.
> Tool-specific files (`CLAUDE.md`, `.github/copilot-instructions.md`) reference or copy this file.
> **Edit here** when architecture or conventions change — do not edit the tool-specific wrappers directly.

## Project Overview

REMSFAL Backend is a multi-module Maven project implementing a microservices architecture using Quarkus for facility management. It works with the remsfal-frontend repository.

## Architecture

### Module Structure

- **remsfal-core**: Core business models (`de.remsfal.core.model`) and API interfaces (`de.remsfal.core.api`) shared across all services
- **remsfal-test**: Shared test utilities and base classes
- **remsfal-common**: Shared implementations for authentication, JWT handling, and common utilities
- **remsfal-platform**: Primary microservice handling user auth, metadata storage, and core CRUD operations (port 8080, PostgreSQL database)
- **remsfal-ticketing**: Document storage and ticketing functionality (port 8081, Cassandra database)
- **remsfal-notification**: Email and notification services (port 8082, Kafka-based)

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
- Use `@Authenticated` annotation on JAX-RS resources requiring login

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

This project follows the **Entity-Control-Boundary (ECB)** pattern:

- **Core interfaces**: `de.remsfal.core.api.*Endpoint` - JAX-RS endpoint interfaces with OpenAPI annotations
- **JSON DTOs**: `de.remsfal.core.json.*` - Immutable DTOs using `@ImmutableStyle` and Jackson
- **Kafka event DTOs**: `de.remsfal.core.json.eventing.*` - Immutable event DTOs for Kafka messaging
- **REST boundary**: `de.remsfal.{service}.boundary.*Resource` - Incoming HTTP endpoints
- **Kafka boundary**: `de.remsfal.{service}.boundary.eventing.*Consumer` / `*Producer` - Kafka consumers (incoming) and producers (outgoing)
- **Business logic**: `de.remsfal.{service}.control.*Controller` - Orchestration and business rules
- **Data access**: `de.remsfal.{service}.entity.dao.*Repository` - Data repositories
- **JPA/Cassandra Entities**: `de.remsfal.{service}.entity.dto.*Entity` - All entities end with `Entity` suffix

### ECB Layer Rules

| What | Layer | Package |
|---|---|---|
| REST endpoints | Boundary | `boundary.*Resource` |
| Kafka consumers (incoming) | Boundary | `boundary.eventing.*Consumer` |
| Kafka producers (outgoing) | Boundary | `boundary.eventing.*Producer` |
| Business logic & orchestration | Control | `control.*Controller` |
| JPA / Cassandra entities, value objects | Entity | `entity.dto.*Entity` |

> **Do not place Kafka producers/consumers in `control`** — they are boundary components.
> A `*Controller` that *triggers* Kafka events (e.g. `NotificationController`) stays in `control`;
> the thin producer wrapper that wraps the `Emitter` belongs in `boundary.eventing`.

### Layered Architecture Pattern

When implementing a new feature:

1. Define the API contract in `remsfal-core/src/main/java/de/remsfal/core/api/*Endpoint.java`
2. Create immutable JSON DTOs in `remsfal-core/src/main/java/de/remsfal/core/json/*Json.java` using `@ImmutableStyle`
3. Implement the REST endpoint in `remsfal-services/remsfal-{service}/src/main/java/de/remsfal/service/boundary/*Resource.java`
4. Add business logic in `de.remsfal.{service}.control.*Controller.java`
5. Create repository methods in `de.remsfal.{service}.entity.dao.*Repository.java`
6. Define JPA entities in `de.remsfal.{service}.entity.dto.*Entity.java`
7. For Kafka: place event DTOs in `remsfal-core/.../json/eventing/`, consumers and producers in `boundary/eventing/`

### Data Layer

- **Platform Service**: PostgreSQL with Liquibase migrations in `remsfal-services/remsfal-platform/src/main/resources/META-INF/liquibase-changelog*.xml`
- **Ticketing Service**: Cassandra for document/chat storage with Liquibase migrations in `remsfal-services/remsfal-ticketing/src/main/resources/META-INF/liquibase-changelog*.xml`
- All JPA entities extend from base classes and must end with `Entity` suffix
- JSON DTOs use `@Immutable` with `@ImmutableStyle` annotation and Jackson for serialization

### Testing Patterns

- **Platform service tests**: Extend `AbstractServiceTest` for transaction management
- **Kafka producer tests**: Extend `AbstractKafkaTest`, inject the producer, call the send method, assert via `given().topic(...).assertThat().json(...)`
- **Kafka consumer tests**: Use `@QuarkusTest` + `AbstractKafkaTest`, register serde in overridden `clearAllTopics()`, produce via `companion.produce(...).fromRecords(...).awaitCompletion()`, verify side effects with `@InjectSpy` + `Awaitility`
- **Ticketing Kafka tests**: Use `@QuarkusTestResource(CassandraTestResource.class)` in addition to `KafkaCompanionResource`; re-initialize companion from bootstrap config in `@BeforeEach`
- **Ticketing tests**: Extend `AbstractTicketingTest`
- Use `@QuarkusTest` with appropriate test resources (`KafkaCompanionResource`, `CassandraTestResource`)
- Test data constants should be defined in `TestData` class
- Kafka test classes live in `boundary/eventing/` (same package as the classes under test)

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
- Test coverage > 80% tracked via JaCoCo
- Checkstyle violations fail the build
- Container images published to GitHub Container Registry

## Common Gotchas

- Services communicate via Kafka topics, not direct HTTP calls
- JWT tokens use cookie authentication, not Authorization headers
- Platform service must be running for other services to validate JWTs
- Docker Compose requires newer versions supporting `include:` directive
- Notification service requires SMTP configuration in `.env` file to send emails
- Every JAX-RS endpoint method must declare an explicit `@APIResponse` for its success status code
  (e.g. `200` for GET/PATCH/PUT, `201` for creating POSTs) — if the method has *any* `@APIResponse`
  annotations but none for the success code, SmallRye OpenAPI silently omits the success response
  from the generated spec entirely (no schema, no entry at all), regardless of the method's actual
  return type. A bare `@APIResponse(responseCode = "200", description = "...")` without `content` is
  enough — SmallRye infers the schema from the return type automatically. Only when the method
  returns the generic JAX-RS `Response` type (not a concrete DTO) does the schema need to be spelled
  out explicitly via `content = @Content(mediaType = ..., schema = @Schema(implementation = XxxJson.class))`.

## Observability

- OpenTelemetry integration with Grafana LGTM stack on port 3000
- Kafka UI available on port 8090
- Standard Quarkus health endpoints available
- OpenAPI specs generated in `target/openapi/` for each service

## Adding AI Tool Support

This project uses a **single source of truth** pattern for AI instructions:

- `AGENT.md` (this file) — tool-agnostic, the only file to edit
- `CLAUDE.md` — thin wrapper; instructs Claude Code to read `AGENT.md`
- `.github/copilot-instructions.md` — thin wrapper; instructs GitHub Copilot to read `AGENT.md`

When adding support for a new AI tool (Cursor, Windsurf, Aider, etc.), create a thin wrapper that instructs the tool to read `AGENT.md`, then document it in this section.
