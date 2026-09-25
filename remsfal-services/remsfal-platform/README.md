# REMSFAL Platform Microservice (Backend)

This microservice is the most important microservice, which is responsible for core functionalities such as user login, metadata storage, etc.

## Running the application in dev mode

You can run this microservice in dev mode that enables live coding using:

```shell script
mvn clean install
mvn compile quarkus:dev -pl remsfal-services/remsfal-platform
```

> **_NOTE:_**  Quarkus ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.


## Dev login & seed data

In dev mode no Google credentials are required. The platform replaces the Google login with a simple
dev login and seeds a small, linked sample data set on startup.

**Dev login:** the frontend still calls `/api/v1/authentication/login`, but in dev mode the backend
redirects to `/api/v1/authentication/dev-login` instead of Google. There you pick one of the seed users
or enter any email address; unknown users are created on first login, exactly like after a Google login
(including linking to tenants/contractors with the same email and the welcome email).
To work with several users at the same time, use a private window or a second browser profile.

**Seed data:** on the first start the following data is created through the regular controllers
(so Kafka events and emails are produced as usual):

| User                     | Name           | Role                                                          |
|--------------------------|----------------|---------------------------------------------------------------|
| `verwalter@remsfal.dev`  | Vera Verwalter | Proprietor of project *Musterhaus Berlin*                     |
| `mieter@remsfal.dev`     | Max Mieter     | Tenant of apartment *WE 01* (rental agreement since 2025-01-01) |
| `handwerker@remsfal.dev` | Hanna Handwerk | Owner of *Sanitär Handwerk GmbH*, contractor in the project   |

Seeding is skipped if `verwalter@remsfal.dev` already exists. To seed again, delete the seed users
(or reset the PostgreSQL volume). All emails end up in Mailpit at [http://localhost:8025](http://localhost:8025),
see the [notification service](../remsfal-notification/README.md).

Both features are **build-time switches** that are only enabled in the dev profile, so the classes are
not even part of a production build:

```properties
%dev.de.remsfal.auth.dev-login.enabled=true
%dev.de.remsfal.dev.seed.enabled=true
```

### Testing the real Google login locally (optional)

1. Create your own OAuth client as described in [Creating a Google OAuth client](#creating-a-google-oauth-client)
   with the redirect URI `http://localhost:5173/api/v1/authentication/session`.
2. Put the credentials into `./remsfal-services/remsfal-platform/.env` (ignored by Git):
   ```shell script
   DE_REMSFAL_AUTH_OIDC_CLIENT_ID=<YOUR-ID>.apps.googleusercontent.com
   GOOGLE_CLIENT_SECRET=<YOUR-SECRET>
   ```
3. Start the dev mode with the dev login disabled:
   ```shell script
   mvn compile quarkus:dev -pl remsfal-services/remsfal-platform -Dde.remsfal.auth.dev-login.enabled=false
   ```

## Running the application for production

We recommend using ready-made container images for productive use. A complete list of all available microservices can be found in the [GitHub Container Registry](https://github.com/remsfal/remsfal-backend/pkgs/container/remsfal-backend).

### Configuration

For production you need to configure at least the [database](#database), [Google OAuth](#google-oauth)
and your own [JWT keys](#jwt-token). Configure them via environment
variables (or your deployment's secret store) or as JVM arguments &ndash; **never commit secrets to
[application.properties](src/main/resources/application.properties)**.

#### Production secrets checklist

| Environment variable                            | Description                                                         |
|-------------------------------------------------|---------------------------------------------------------------------|
| `QUARKUS_DATASOURCE_USERNAME` / `_PASSWORD` / `QUARKUS_DATASOURCE_JDBC_URL` | Database connection                   |
| `DE_REMSFAL_AUTH_OIDC_CLIENT_ID`                | Google OAuth client ID                                              |
| `GOOGLE_CLIENT_SECRET`                          | Google OAuth client secret                                          |
| `DE_REMSFAL_AUTH_JWT_PRIVATE_KEY_LOCATION`      | Location of the JWT signing key, e.g. `file:/run/secrets/remsfal_jwt_private_key` (see [JWT Token](#jwt-token)) |
| `QUARKUS_MAILER_*`                              | SMTP settings, see the [notification service](../remsfal-notification/README.md#mailer-configuration) |

#### database

Adjust the configuration for your database, don't use the provided ones in production!

```properties
quarkus.datasource.username=remsfaladmin
quarkus.datasource.password=remsfalSecret
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/REMSFAL
quarkus.datasource.devservices.enabled=false
```

Or use JVM arguments

```sh
java -Dquarkus.datasource.username=remsfaladmin \
     -Dquarkus.datasource.password=remsfalSecret \
     -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/REMSFAL \
     -Dquarkus.datasource.devservices.enabled=false \
     -jar remsfal-services/remsfal-platform/target/remsfal-platform-runner.jar
```

#### Google OAuth

Users log in with their Google account via OpenID Connect. You need your own OAuth client for every
deployment (see below). Provide the credentials as environment variables:

```shell script
DE_REMSFAL_AUTH_OIDC_CLIENT_ID=<YOUR-ID>.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=<YOUR-SECRET>
```

> **_WARNING:_** Never commit the client secret. Google scans public repositories and reports leaked
> secrets; a leaked secret must be rotated immediately (see step 6).

##### Creating a Google OAuth client

1. Open the [Google Cloud Console](https://console.cloud.google.com/) and create or select a project.
2. Configure the consent screen under **Google Auth Platform**:
   - **Branding:** app name (e.g. *REMSFAL*), user support email, app logo (optional), and your domain
     (e.g. `remsfal.de`) as authorized domain.
   - **Audience:** user type *External*. While the app is in *Testing* status, only the listed test users
     can log in; publish the app for production use.
   - **Data access:** the scopes `openid` and `.../auth/userinfo.email` are sufficient.
3. Under **Clients** click **Create client**, choose the application type **Web application** and add the
   authorized redirect URI of your deployment:
   `https://<your-host>/api/v1/authentication/session`
   (for a local test of the real Google login: `http://localhost:5173/api/v1/authentication/session`).
4. Copy the client ID and the client secret. Download the JSON or store the secret in your password
   manager right away &ndash; it is only shown once.
5. Configure the platform service with `DE_REMSFAL_AUTH_OIDC_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`
   (e.g. as GitHub Actions / Kubernetes secrets of your deployment).
6. **Rotating the secret:** open the client, click **Add secret**, deploy the new secret, then disable
   and delete the old one. This keeps the login working without downtime.

#### JWT Token

The platform signs the access and refresh tokens (session cookies) with an RSA private key. All services &ndash;
including the platform itself &ndash; verify the tokens with the matching public key, which the platform publishes
at `GET /api/v1/authentication/jwks`. The private key is the most important secret of a deployment: whoever
has it can issue tokens with arbitrary roles.

The key is **never part of the build or the container image**. The platform expects it at runtime and does not
start without it. Only the private key is needed; the public key is derived from it.

| Property                                     | Environment variable                          | Description                                  |
|----------------------------------------------|-----------------------------------------------|----------------------------------------------|
| `de.remsfal.auth.jwt.private-key-location`   | `DE_REMSFAL_AUTH_JWT_PRIVATE_KEY_LOCATION`    | `file:/path/to/key.pem` (or `classpath:…`)   |
| `de.remsfal.auth.jwt.key-id`                 | `DE_REMSFAL_AUTH_JWT_KEY_ID`                  | Key ID (`kid`) in the tokens and the JWKS    |

The other services verify the tokens via the JWKS endpoint of the platform:

```shell script
MP_JWT_VERIFY_PUBLICKEY_LOCATION=http://<platform-host>:8080/api/v1/authentication/jwks
```

##### Generating a key

Every installation needs its own key. Generate an RSA key in PKCS#8 format:

```sh
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out privateKey.pem
```

> **_NOTE:_** Keys starting with `-----BEGIN RSA PRIVATE KEY-----` (PKCS#1) are not supported.
> Convert them with `openssl pkcs8 -topk8 -nocrypt -in old.pem -out privateKey.pem`.

##### Community Edition (Docker Compose)

Mount the key as [Compose secret](https://docs.docker.com/compose/how-tos/use-secrets/). The containers run as
user `185`, so the key file must be readable for this user:

```sh
mkdir -p secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt_private_key.pem
sudo chown 185 secrets/jwt_private_key.pem && sudo chmod 400 secrets/jwt_private_key.pem
```

```yaml
services:
  remsfal-platform:
    image: ghcr.io/remsfal/remsfal-platform:latest
    secrets:
      - remsfal_jwt_private_key
    environment:
      DE_REMSFAL_AUTH_JWT_PRIVATE_KEY_LOCATION: file:/run/secrets/remsfal_jwt_private_key
      # ... database, Google OAuth, etc.

  remsfal-ticketing:
    image: ghcr.io/remsfal/remsfal-ticketing:latest
    environment:
      MP_JWT_VERIFY_PUBLICKEY_LOCATION: http://remsfal-platform:8080/api/v1/authentication/jwks

secrets:
  remsfal_jwt_private_key:
    file: ./secrets/jwt_private_key.pem
```

##### Enterprise Edition (Kubernetes)

Store the key as Kubernetes secret (or manage it with e.g. External Secrets or Sealed Secrets):

```sh
kubectl create secret generic remsfal-jwt --from-file=privateKey.pem
```

Mount it into the platform pod. Kubernetes mounts secret files as `root`, so grant read access via `fsGroup`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: remsfal-platform
spec:
  template:
    spec:
      securityContext:
        fsGroup: 185
      containers:
        - name: remsfal-platform
          image: ghcr.io/remsfal/remsfal-platform:latest
          env:
            - name: DE_REMSFAL_AUTH_JWT_PRIVATE_KEY_LOCATION
              value: file:/run/secrets/remsfal-jwt/privateKey.pem
          volumeMounts:
            - name: jwt-key
              mountPath: /run/secrets/remsfal-jwt
              readOnly: true
      volumes:
        - name: jwt-key
          secret:
            secretName: remsfal-jwt
            defaultMode: 0440
```

The ticketing deployment gets
`MP_JWT_VERIFY_PUBLICKEY_LOCATION=http://remsfal-platform:8080/api/v1/authentication/jwks`
(using the name of the platform service).

##### Rotating the key

1. Generate a new key and replace the secret (Compose: the file, Kubernetes: `kubectl create secret … --dry-run=client -o yaml | kubectl apply -f -`).
2. Set a new key ID, e.g. `DE_REMSFAL_AUTH_JWT_KEY_ID=remsfal-2026-09`.
3. Restart the platform, then the other services, so that they load the new public key from the JWKS endpoint.

All existing sessions become invalid; users have to log in again.

##### Local development

In dev mode no key has to be configured: on the first start the platform generates a key in
`remsfal-services/remsfal-platform/.dev-keys/` (ignored by Git) and reuses it afterwards, so sessions survive
restarts. Delete the directory to get a new key. Tests use the fixed key pair in `src/test/resources/test-keys/`.

#### Cookie Configuration

The platform service uses separate paths for access and refresh token cookies to enhance security:

- **Access Token** (`remsfal_access_token`): Path set to `/api` to allow sharing across microservices
- **Refresh Token** (`remsfal_refresh_token`): Path set to `/api/v1/authentication` to restrict to platform service only

This configuration prevents refresh tokens from being sent to other microservices running on different ports (e.g., ticketing service on port 8081). Only the platform service can receive and process refresh tokens, while all services can validate access tokens.

Default configuration (can be overridden in application.properties):
```properties
de.remsfal.auth.access-token.cookie-path=/api
de.remsfal.auth.refresh-token.cookie-path=/api/v1/authentication
```

#### Run

To package and execute the application

```sh
./mvnw package
java -jar remsfal-services/remsfal-platform/target/remsfal-platform-runner.jar
```

After execution `remsfal-backend` will be available under [`https://localhost:8080/api`](https://localhost:8080/api).
