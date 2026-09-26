<img src="https://remsfal.de/remsfal-logo.svg" alt="REMSFAL Logo" width="60%">

![GitHub Release](https://img.shields.io/github/v/release/remsfal/remsfal-backend?label=latest%20release)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=remsfal_remsfal-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=remsfal_remsfal-backend)
[![License](https://img.shields.io/github/license/remsfal/remsfal-backend)](https://github.com/remsfal/remsfal-backend/blob/main/LICENSE)
![Contributors](https://img.shields.io/github/contributors/remsfal/remsfal-backend)

# Open Source Facility Management Software (Backend)

`remsfal-backend` is a backend service built using Java and Quarkus framework to manage real estate projects.
It works together with the [`remsfal-frontend`](https://github.com/remsfal/remsfal-frontend) repository.  
You can see a live version at https://remsfal.online.


## Architecture

The project is structured into multiple modules:

- **[remsfal-core](remsfal-core/README.md)**: A library containing core business models and API interfaces.
- **[remsfal-gaeb](remsfal-gaeb/README.md)**: A library providing XML bindings for the GAEB DA XML standard 3.3 (German construction industry data exchange).
- **[remsfal-common](remsfal-services/remsfal-common/README.md)**: A library containing implementations used in all microservices.
- **[remsfal-ticketing](remsfal-services/remsfal-ticketing/README.md)**: A microservice responsible for ticketing system functionalities and document storage.
- **[remsfal-notification](remsfal-services/remsfal-notification/README.md)**: A microservice responsible for all customer notifications.
- **[remsfal-platform](remsfal-services/remsfal-platform/README.md)**: The most important microservice, which is responsible for core functionalities such as user login, metadata storage, etc.

> **_NOTE:_** Before implementing a new feature, please **first** read [AGENT.md](AGENT.md) to understand the project architecture, layered patterns, and coding conventions. For authentication and authorization concepts (RBAC, roles, JWT structure), see [Authorization.md](Authorization.md). For how user accounts get linked to tenants, projects, and organizations, and the related data protection considerations, see [AccountLinking.md](AccountLinking.md).


## Development

This project uses MAVEN to build and test the complete backend microservice architecture.
Before you change anything, make sure that your development setup meets the requirements as described in [Prerequisites](#prerequisites).
Afterwards you can build and package the application by running:

```shell script
mvn package
```

### Prerequisites

To develop or start this project locally, you need

- Java 17 or higher
- Maven 3.8.1 or higher
- Docker or Podman to start containers


## How to get started

For ease of use its recommended to run the infrastructure (PostgreSQL, Kafka, Cassandra, LocalStack, OCR, Grafana, Mailpit)
using the provided [docker-compose.yml](docker-compose.yml).

```sh
docker compose up -d
```

**Important:** The [`docker-compose.yml`](docker-compose.yml) file uses the `include:` directive, which is only supported in
newer versions of Docker Compose. Please make sure your Docker Desktop or Docker Compose CLI is up to date.

| Container   | Port(s)                     | Purpose                                     |
|-------------|-----------------------------|---------------------------------------------|
| postgres    | 5432                        | Database of the platform service            |
| cassandra   | 9042                        | Database of the ticketing service           |
| kafka-broker / kafka-ui | 39092 / [8090](http://localhost:8090) | Event streaming           |
| localstack  | 9000                        | S3 compatible file storage                  |
| ocr         | &ndash;                     | OCR service used by the ticketing service   |
| otel-lgtm   | [3000](http://localhost:3000) | Grafana (logs, metrics, traces)           |
| mailpit     | 2525 (SMTP) / [8025](http://localhost:8025) (UI) | Catches all emails in dev mode |

### Local setup without credentials

You do **not** need Google OAuth credentials or an SMTP account to develop locally:

- **Login:** in dev mode the platform service replaces the Google login with a dev login page. Choose one of
  the seed users or enter any email address.
- **Seed data:** on the first start the platform service creates a sample project with linked users:

  | User                     | Role                                                        |
  |--------------------------|-------------------------------------------------------------|
  | `verwalter@remsfal.dev`  | Property manager, proprietor of project *Musterhaus Berlin* |
  | `mieter@remsfal.dev`     | Tenant of apartment *WE 01*                                 |
  | `handwerker@remsfal.dev` | Owner of the contractor *Sanitär Handwerk GmbH*             |

  Use a private window or a second browser profile to be logged in as several users at the same time.
- **Emails:** all emails are caught by Mailpit at [http://localhost:8025](http://localhost:8025).

Details: [platform service](remsfal-services/remsfal-platform/README.md#dev-login--seed-data) (dev login, seed data,
Google OAuth for production) and [notification service](remsfal-services/remsfal-notification/README.md)
(Mailpit, sending real emails, SMTP for production).

### CI/CD

This project utilizes Github Actions to check the code quality
using [SonarCloud](https://sonarcloud.io/summary/new_code?id=remsfal_remsfal-backend&branch=main) therefore its
mandatory to pass the specified **Quality Gates** before a pull request can be merged.

### Start in dev mode

At first you will need to start the infrastructure as described in [How to get started](#how-to-get-started).

Next build the project and start the services you need (each in its own terminal):

```sh
./mvnw clean install
./mvnw compile quarkus:dev -pl remsfal-services/remsfal-platform      # port 8080
./mvnw compile quarkus:dev -pl remsfal-services/remsfal-ticketing     # port 8081
./mvnw compile quarkus:dev -pl remsfal-services/remsfal-notification  # port 8082
```

Then start the [frontend](https://github.com/remsfal/remsfal-frontend) and click *Login* &ndash; you will see
the dev login page described in [Local setup without credentials](#local-setup-without-credentials).

It will automatically recompile when you change something.

### Stylecheck

This project uses [Checkstyle](https://github.com/checkstyle/checkstyle) for code formatting. Please ensure your code
adheres to the style defined in the [checkstyle.xml](src/main/style/checkstyle.xml).

To run the stylecheck use the following command:

```sh
./mvnw checkstyle:checkstyle
```

## Contributing

Before contributing to REMSFAL, please read our [contributing guidelines](https://github.com/remsfal/.github/blob/main/CONTRIBUTING.md). Participation in the REMSFAL project is governed by the [CNCF Code of Conduct](https://github.com/cncf/foundation/blob/main/code-of-conduct.md).

> **_NOTE:_** When contributing to this repository, please **first** discuss the change you wish to make by creating an issue before making a change. Once you got feedback on your idea, feel free to fork the project and open a pull request.

> **_NOTE:_** Please only make changes in files directly related to your issue!


## License

* [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
