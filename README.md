# remsfal-backend
## DB starten
```sh
docker compose up -d
```

## Backend starten

```sh
mvn clean install
mvn compile quarkus:dev -pl remsfal-service
```

