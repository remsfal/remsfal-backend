![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=remsfal_remsfal-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=remsfal_remsfal-backend)
![Contributors](https://img.shields.io/github/contributors/remsfal/remsfal-backend)  
![Remsfal Logo](https://remsfal.de/assets/logo-f578a7d9.png)
# remsfal-backend 

`remsfal-backend` is a backend service built using Java and Quarkus framework to manage real estate projects.
It works together with the [`remsfal-frontend`](https://github.com/remsfal/remsfal-frontend) repository.  
You can see a live version at https://remsfal.de.

## Prerequisits
You will need 
- Java 17 or higher  
- Maven 3.8.1 or higher
-  a **mysql** database


## How to get started

For ease of use its recommended to run mysql using the provided [docker-compose.yml](docker-compose.yml).

```sh
docker compose up -d
```

### Configuration
Furthermore you will need to configurate at least the [database](#database) and [Google Oauth](#google-oauth) to run the application in [application.properties](remsfal-service/src/main/resources/application.properties) or specific them directly as JVM argument.  

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
You will also need to provide your own secrets for [Google OAuth](https://developers.google.com/identity/protocols/oauth2?hl=de). As mentioned before you may also use JVM arguments.
```properties
de.remsfal.auth.oidc.client-id=<YOUR-ID>.apps.googleusercontent.com
de.remsfal.auth.oidc.client-secret=<YOUR-SECRET>
de.remsfal.auth.session.secret=<YOUR-CUSTOM-SESSION-SECRET>
```


#### Camunda run 
Camunda is used for the workflow management. You can run it using the provided docker-compose.yml. 

You can read more about the camunda engine [here](https://docs.camunda.org/manual/latest/user-guide/camunda-bpm-run).

Camunda is running as a separate service in a docker container under the port 8081.

It is started among the other services using the provided docker-compose.yml.

```sh
docker compose up -d
```

Once started Camunda copies the bpmn files from ./processes to the camunda engine.

There is a test class [CamundaApiTest](remsfal-service/src/test/java/de/remsfal/core/CamundaApiTest.java) which is showcasing how the camunda engine can be used. 

To run this test class you will need to start the camunda engine using the provided docker-compose.yml.

You can run the test class using the following command:

```sh
 mvn test -Dgroups=camunda
```


You can access the camunda cockpit in browser under [`http://localhost:8081`](http://localhost:8081) with the credentials `demo/demo`.



To interact with the camunda engine you can use the REST API. 

You can read more about the camunda REST API [here](https://docs.camunda.org/manual/latest/reference/rest/).

For example to start a process you can use the following endpoint:

POST: http://localhost:8081/engine-rest/process-definition/key/{process-key}/start


To get all open tasks you can use the following endpoint:

GET: http://localhost:8081/engine-rest/task


To complete a task you can use the following endpoint:

POST: http://localhost:8081/engine-rest/task/{id}/complete

You can find the id of the task in the response of the get task request.

The implementation of the camunda engine is in a proof of concept state and is not integrated into the application. 

#### Run 

To package and execute the application
```sh
mvn package
java -jar remsfal-service/target/remsfal-service-runner.jar
```

After execution `remsfal-backend` will be available under [`https://localhost:8080/api`](https://localhost:8080/api).


## Contributing
When contributing to this repository, please **first** discuss the change you wish to make by creating an issue before making a change.

Once you got feedback on your idea feel free to fork the project and open a pull request.

Please only make changes in files directly related to your issue.

This project uses [Checkstyle](https://github.com/checkstyle/checkstyle) for code formatting. Please ensure your code adheres to the style defined in the [checkstyle.xml](src/main/style/checkstyle.xml).

### CI/CD
This project utilizes Github Actions to check the code quality using [SonarCloud](https://sonarcloud.io/summary/new_code?id=remsfal_remsfal-backend&branch=main) therefore its mandatory to pass the specified **Quality Gates** before a pull request can be merged.


### Development

The project is structured into multiple modules:

**remsfal-core**: Contains core business logic and API interfaces.  
**remsfal-service**: Implements the REST API and application services.  


At first you well need to start the db as described in [Prerequisits](#prerequisits).

Next run the project using the following command:
```sh
mvn clean install
mvn compile quarkus:dev -pl remsfal-service
```
It will automatically recompile when you change something.

### Stylecheck

To run the stylecheck use the following command:
```sh
mvn checkstyle:checkstyle
```

## Copyright
All licenses in this repository are copyrighted by their respective authors.   
Everything else is released under Apache 2.0. See [LICENSE](https://github.com/remsfal/remsfal-backend?tab=Apache-2.0-1-ov-file#readme) for details.

