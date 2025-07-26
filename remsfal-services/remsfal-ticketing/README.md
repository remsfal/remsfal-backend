# REMSFAL Ticketing Microservice (Backend)

This microservice is responsible for issues, defects, tasks, and chat functionalities.

## Running the application in dev mode

You can run this microservice in dev mode that enables live coding using:

```shell script
mvn clean install
mvn compile quarkus:dev -pl remsfal-services/remsfal-ticketing
```

> **_NOTE:_**  Quarkus ships with a Dev UI, which is available in dev mode only at http://localhost:8081/q/dev/.


## Running the application for production

We recommend using ready-made container images for productive use. A complete list of all available microservices can be found in the [GitHub Container Registry](https://github.com/remsfal/remsfal-backend/pkgs/container/remsfal-backend).
