# REMSFAL Notification Microservice (Backend)

This microservice is responsible for all customer notification functionalities.

## Service-specific Prerequisites

Since the notification microservice is designed to send emails, an outgoing SMTP server must be configured before starting.
The most important mailing configuration can be found in the [Quarkus Mailer Configuration Reference](https://quarkus.io/guides/mailer-reference#configuration-reference). Since passwords must not be included in the Git version control system, passwords should not be stored in the `application.properties` file. Therefore, the mailing configuration should be saved via an [alternative method](https://quarkus.io/guides/config-reference#env-file).

We recommend saving the mailing configuration in an `.env` file. You should place this `.env` file directly in the sub-module directory under `./remsfal-services/remsfal-notification/.env`.

Below you will find an example of how to configure an HTW account in an `.env` file:

```shell script
QUARKUS_MAILER_FROM=FirstName.LastName@htw-berlin.de
QUARKUS_MAILER_USERNAME=s0000001
QUARKUS_MAILER_PASSWORD=<your password>
QUARKUS_MAILER_HOST=mail.htw-berlin.de
QUARKUS_MAILER_PORT=465
QUARKUS_MAILER_SSL=true
_DEV_QUARKUS_MAILER_MOCK=false
```
> **_NOTE:_**  Use your HTW account username (e.g. s0000001 or mustermann; but not your email alias FirstName.LastName).


## Running the application in dev mode

You can run this microservice in dev mode that enables live coding using:

```shell script
mvn clean install
mvn compile quarkus:dev -pl remsfal-services/remsfal-notification
```

> **_NOTE:_**  Quarkus ships with a Dev UI, which is available in dev mode only at http://localhost:8082/q/dev/.


## Running the application for production

We recommend using ready-made container images for productive use. A complete list of all available microservices can be found in the [GitHub Container Registry](https://github.com/remsfal/remsfal-backend/pkgs/container/remsfal-backend).
