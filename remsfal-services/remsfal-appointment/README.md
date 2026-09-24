# REMSFAL Appointment Microservice (Backend)

This microservice provides provider-independent appointment scheduling with slot-based booking, conflict detection, and working-hours validation.

## Running the application in dev mode

```shell script
mvn clean install
mvn compile quarkus:dev -pl remsfal-services/remsfal-appointment
```

> **_NOTE:_** Quarkus ships with a Dev UI at http://localhost:8083/q/dev/ (dev mode only).

## Running the application for production

We recommend using ready-made container images for productive use. A complete list of all available microservices can be found in the [GitHub Container Registry](https://github.com/remsfal/remsfal-backend/pkgs/container/remsfal-backend).


## Configuration

Configure the database in `remsfal-backend/remsfal-services/remsfal-appointment/src/main/resources/application.properties` or pass them as JVM args.

```properties
quarkus.datasource.username=remsfaladmin
quarkus.datasource.password=remsfalSecret
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/REMSFAL
quarkus.datasource.devservices.enabled=false
```

```sh
java -Dquarkus.datasource.username=remsfaladmin \
     -Dquarkus.datasource.password=remsfalSecret \
     -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/REMSFAL \
     -Dquarkus.datasource.devservices.enabled=false \
     -jar target/remsfal-appointment-runner.jar
```

## Features

- Slot calculation with 15-minute granularity and working-hours/break constraints
- Conflict detection to prevent double-booking per craftsman
- Appointment lifecycle tracking (OPEN, CONFIRMED, DECLINED, CANCELLED)
- iCalendar (.ics) export for confirmed appointments

## API Endpoints (base path: `/api/appointments`)

### Create an appointment request
```http
POST /api/appointments
Content-Type: application/json

{
  "craftsmanId": "craftsman-123",
  "resourceId": "property-456",
  "type": "VIEWING",
  "durationMinutes": 60,
  "from": "2025-12-27T09:00:00",
  "to": "2025-12-27T17:00:00",
  "workingHours": {
    "start": "08:00:00",
    "end": "17:00:00",
    "breaks": [
      {"start": "12:00:00", "end": "13:00:00"}
    ]
  }
}
```

### Get available slots
```http
GET /api/appointments/{id}/slots
```

### Book an appointment
```http
POST /api/appointments/{id}/book
Content-Type: application/json

{
  "slotStart": "2025-12-27T09:00:00"
}
```

### Cancel an appointment
```http
POST /api/appointments/{id}/cancel
Content-Type: application/json

{
  "reason": "Client requested reschedule"
}
```

### Export to iCalendar
```http
GET /api/appointments/{id}/ical
Accept: text/calendar
```

For full API details, visit Swagger UI at http://localhost:8083/q/swagger-ui when running in dev mode.

## Database

Flyway migrations manage the schema, including:
- `appointment`
- `working_hours`
- `break_time`

## Testing

```shell script
mvn test
```