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

#### Zeebe run 

Zeebe is a workflow engine for orchestrating microservices using BPMN 2.0 processes. This project integrates Zeebe with Camunda 8 BPMN Diagrams.

You can model your BPMN workflows in the Camunda Modeler. Please ensure that you are using the Camunda 8 BPMN Diagrams.

Make sure the IDs and job types of the tasks are defined, as they will be used by the JobWorkers later. You can find the task IDs in the XML or set them in the Modeler. The task types must match the task types in the BPMN file. You can set them in the Modeler under Task definition → Task type.

The Zeebe client automatically deploys the BPMN files to the Zeebe broker. It is configured in the application.properties file. Place your BPMN workflow files in the remsfal-service/src/main/resources/processes directory, and they will be automatically deployed to Zeebe.

You can start the process using the controller (see the example in ZeebeController.java). The controller will start the process with the given variables.

The Controller for starting the process is configured in ZeebeController.java. The controller will start the given process with the given variables. They will be passed to the process as JSON. For example, the ticket-process.bpmn file has the processID "ticket-process" and a variable "approve". The JSON should look like this: 

{
  "processId": "ticket-process",
  "variables": {
    "approve": true
  }
}

The JobWorkers will automatically pick up the tasks and execute them.
The JobWorkers are configured in ZeebeWorker.java. The worker will pick up the tasks with the given task type and execute them. They are annotated with @ZeebeWorker. The task type must match the task type in the BPMN file. When the worker picks up the task, it will execute the given function. After the function is executed, the worker will complete the task and pass the result back to the Zeebe broker. Then the next task will be picked up by the worker. 

This setup provides a foundational integration of Zeebe into your project.

To adapt this for your own workflow:

Create a new BPMN file to reflect your process.
Update your JobWorkers to handle the tasks specific to your workflow.



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
