<img src="https://remsfal.de/logo_upscaled.png" width="60%">

![GitHub Release](https://img.shields.io/github/v/release/remsfal/remsfal-backend?label=latest%20release)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=remsfal_remsfal-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=remsfal_remsfal-backend)
![Contributors](https://img.shields.io/github/contributors/remsfal/remsfal-backend)

# Open Source Facility Management Software (Backend)

`remsfal-backend` is a backend service built using Java and Quarkus framework to manage real estate projects.
It works together with the [`remsfal-frontend`](https://github.com/remsfal/remsfal-frontend) repository.  
You can see a live version at https://remsfal.de.

## Prerequisits

You will need

- Java 17 or higher
- Maven 3.8.1 or higher
- a **mysql** database

## How to get started

For ease of use its recommended to run mysql using the provided [docker-compose.yml](docker-compose.yml).

```sh
docker compose up -d
```

### Configuration

Furthermore you will need to configurate at least the [database](#database) and [Google Oauth](#google-oauth) to run the
application in [application.properties](remsfal-service/src/main/resources/application.properties) or specific them
directly as JVM argument.

#### database

Adjust the configuration for your database, don't use the provided ones in production!

```properties
quarkus.datasource.username=root
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/REMSFAL
quarkus.datasource.devservices.enabled=false
```

Or use JVM agruments

```sh
java -Dquarkus.datasource.username=root \
     -Dquarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/REMSFAL \
     -Dquarkus.datasource.devservices.enabled=false \
     -jar remsfal-service/target/remsfal-service-runner.jar
```

#### Google OAuth

You will also need to provide your own secrets
for [Google OAuth](https://developers.google.com/identity/protocols/oauth2?hl=de). As mentioned before you may also use
JVM arguments.

```properties
de.remsfal.auth.oidc.client-id=<YOUR-ID>.apps.googleusercontent.com
de.remsfal.auth.oidc.client-secret=<YOUR-SECRET>
de.remsfal.auth.session.secret=<YOUR-CUSTOM-SESSION-SECRET>
```

## JWT Token

For the JWT token its highly recommended to replace the default private key and public key with your own.

### Generate new keys in PEM format using openssl

```sh
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
```

#### Run

To package and execute the application

```sh
./mvnw package
java -jar remsfal-service/target/remsfal-service-runner.jar
```

After execution `remsfal-backend` will be available under [`https://localhost:8080/api`](https://localhost:8080/api).

## Contributing

When contributing to this repository, please **first** discuss the change you wish to make by creating an issue before
making a change.

Once you got feedback on your idea feel free to fork the project and open a pull request.

Please only make changes in files directly related to your issue.

This project uses [Checkstyle](https://github.com/checkstyle/checkstyle) for code formatting. Please ensure your code
adheres to the style defined in the [checkstyle.xml](src/main/style/checkstyle.xml).

### CI/CD

This project utilizes Github Actions to check the code quality
using [SonarCloud](https://sonarcloud.io/summary/new_code?id=remsfal_remsfal-backend&branch=main) therefore its
mandatory to pass the specified **Quality Gates** before a pull request can be merged.

### Development

The project is structured into multiple modules:

**remsfal-core**: Contains core business logic and API interfaces.  
**remsfal-service**: Implements the REST API and application services.

At first you well need to start the db as described in [Prerequisits](#prerequisits).

Next run the project using the following command:

```sh
./mvnw clean install
./mvnw compile quarkus:dev -pl remsfal-service
```

It will automatically recompile when you change something.

### Stylecheck

To run the stylecheck use the following command:

```sh
./mvnw checkstyle:checkstyle
```

## Copyright

All licenses in this repository are copyrighted by their respective authors.   
Everything else is released under Apache 2.0.
See [LICENSE](https://github.com/remsfal/remsfal-backend?tab=Apache-2.0-1-ov-file#readme) for details.

