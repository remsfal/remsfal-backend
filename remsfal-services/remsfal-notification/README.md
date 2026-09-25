# REMSFAL Notification Microservice (Backend)

This microservice is responsible for all customer notification functionalities.

## Service-specific Prerequisites

Since the notification microservice is designed to send emails, it needs an outgoing SMTP server.
**For local development no SMTP account is required:** in dev mode all emails are delivered to
[Mailpit](https://mailpit.axllent.org/), which is started together with the other infrastructure
containers (`docker compose up -d` in the repository root, see [docker-compose.yml](docker-compose.yml)).

The most important mailing configuration can be found in the
[Quarkus Mailer Configuration Reference](https://quarkus.io/guides/mailer-reference#configuration-reference).

### Mailpit (default in dev mode)

Mailpit is a local SMTP server that catches **every** outgoing email, regardless of the recipient address,
and shows it in a web UI:

| What        | Where                                          |
|-------------|------------------------------------------------|
| Web UI      | [http://localhost:8025](http://localhost:8025) |
| SMTP server | `localhost:2525` (no TLS, no authentication)   |

In the web UI you can inspect the HTML and plain text versions of each email, check links, view the
raw source and headers, and delete messages. Because nothing is delivered to real mailboxes, you can
use the fictitious seed users of the platform service (`…@remsfal.dev`) and still see every notification
they would receive.

The dev defaults in [application.properties](src/main/resources/application.properties) are:

```properties
%dev.quarkus.mailer.from=no-reply@remsfal.dev
%dev.quarkus.mailer.mock=false
%dev.quarkus.mailer.host=localhost
%dev.quarkus.mailer.port=2525
%dev.quarkus.mailer.tls=false
```

### Sending real emails in dev mode (testing)

To check how emails look in real mail clients, you can point the dev mode at a real SMTP server.
Passwords must never be committed, so put the configuration into an
[`.env` file](https://quarkus.io/guides/config-reference#env-file) at
`./remsfal-services/remsfal-notification/.env` (`.env` files are ignored by Git).
Values from the `.env` file take precedence over the Mailpit defaults in `application.properties`.

Example for an HTW account:

```shell script
QUARKUS_MAILER_FROM=FirstName.LastName@htw-berlin.de
QUARKUS_MAILER_USERNAME=s0000001
QUARKUS_MAILER_PASSWORD=<your password>
QUARKUS_MAILER_HOST=mail.htw-berlin.de
QUARKUS_MAILER_PORT=465
QUARKUS_MAILER_TLS=true
```
> **_NOTE:_**  Use your HTW account username (e.g. s0000001 or mustermann; but not your email alias FirstName.LastName).

Example for a Gmail account (requires 2-step verification and an
[app password](https://support.google.com/accounts/answer/185833)):

```shell script
QUARKUS_MAILER_FROM=your.name@gmail.com
QUARKUS_MAILER_USERNAME=your.name@gmail.com
QUARKUS_MAILER_PASSWORD=<16 character app password>
QUARKUS_MAILER_HOST=smtp.gmail.com
QUARKUS_MAILER_PORT=587
QUARKUS_MAILER_TLS=false
QUARKUS_MAILER_START_TLS=REQUIRED
```

> **_WARNING:_** With a real SMTP server, emails are sent to real recipients. The seed users
> (`…@remsfal.dev`) are not deliverable. To receive the emails yourself, log in through the dev login with
> your own email address (the dev login accepts any address).

Remove or rename the `.env` file to switch back to Mailpit.

> **_NOTE:_** `QUARKUS_MAILER_SSL` is deprecated, use `QUARKUS_MAILER_TLS` instead. If you still have an
> `.env` file from an older setup that contains `_DEV_QUARKUS_MAILER_MOCK=false`, it is no longer needed.

## Running the application in dev mode

You can run this microservice in dev mode that enables live coding using:

```shell script
mvn clean install
mvn compile quarkus:dev -pl remsfal-services/remsfal-notification
```

> **_NOTE:_**  Quarkus ships with a Dev UI, which is available in dev mode only at http://localhost:8082/q/dev/.


## Running the application for production

We recommend using ready-made container images for productive use. A complete list of all available microservices can be found in the [GitHub Container Registry](https://github.com/remsfal/remsfal-backend/pkgs/container/remsfal-backend).

### Mailer configuration

In production, configure the SMTP server via environment variables (or your deployment's secret store).
Never put credentials into `application.properties`.

| Environment variable           | Example                  | Description                                        |
|--------------------------------|--------------------------|----------------------------------------------------|
| `QUARKUS_MAILER_FROM`          | `no-reply@remsfal.de`    | Sender address                                     |
| `QUARKUS_MAILER_HOST`          | `smtp.example.org`       | SMTP host                                          |
| `QUARKUS_MAILER_PORT`          | `465` or `587`           | SMTP port                                          |
| `QUARKUS_MAILER_TLS`           | `true` (port 465)        | Implicit TLS                                       |
| `QUARKUS_MAILER_START_TLS`     | `REQUIRED` (port 587)    | STARTTLS, use instead of `TLS` for port 587        |
| `QUARKUS_MAILER_USERNAME`      | `no-reply@remsfal.de`    | SMTP user                                          |
| `QUARKUS_MAILER_PASSWORD`      | *(secret)*               | SMTP password                                      |

Make sure that the sender domain is allowed to send via your SMTP server (SPF record) and that the
server signs outgoing emails (DKIM); otherwise notifications are likely to end up in spam folders.
