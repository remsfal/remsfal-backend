# remsfal-backend
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Contributors](https://img.shields.io/github/contributors/remsfal/remsfal-backend)

## DB starten
```sh
docker compose up -d
```

## Backend starten

```sh
mvn clean install
mvn compile quarkus:dev -pl remsfal-service
```

