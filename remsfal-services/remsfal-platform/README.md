# REMSFAL Platform Microservice (Backend)

This microservice is the most important microservice, which is responsible for core functionalities such as user login, metadata storage, etc.

## Running the application in dev mode

You can run this microservice in dev mode that enables live coding using:

```shell script
mvn clean install
mvn compile quarkus:dev -pl remsfal-services/remsfal-platform
```

> **_NOTE:_**  Quarkus ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.


## Running the application for production

We recommend using ready-made container images for productive use. A complete list of all available microservices can be found in the [GitHub Container Registry](https://github.com/remsfal/remsfal-backend/pkgs/container/remsfal-backend).

### Configuration

Furthermore you will need to configurate at least the [database](#database) and [Google Oauth](#google-oauth) to run the
application in [application.properties](remsfal-service/src/main/resources/application.properties) or specific them
directly as JVM argument.

#### database

Adjust the configuration for your database, don't use the provided ones in production!

```properties
quarkus.datasource.username=remsfal
quarkus.datasource.password=remsfal
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/REMSFAL
quarkus.datasource.devservices.enabled=false
```

Or use JVM arguments

```sh
java -Dquarkus.datasource.username=remsfal \
     -Dquarkus.datasource.password=remsfal \
     -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/REMSFAL \
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

#### JWT Token

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
