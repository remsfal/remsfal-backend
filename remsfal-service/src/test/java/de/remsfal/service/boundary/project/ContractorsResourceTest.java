package de.remsfal.service.boundary.project;

import de.remsfal.core.model.project.ChatMessageModel;
import de.remsfal.core.model.project.TaskModel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.remsfal.service.TestData;

import static de.remsfal.service.boundary.project.ChatResourceTest.TASK_ID_1;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;

@QuarkusTest
class ContractorsResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/contractors/{ownerId}";
    static final String TASKS_PATH = BASE_PATH + "/tasks";
    static final String TASK_PATH = BASE_PATH + "/task/{taskId}";
    static  final String Task_ID_1= UUID.randomUUID().toString();


    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASKS_PATH })
    void getTasks_FAILED_noAuthentication(String path) {
        given()
                .when()
                .get(TASKS_PATH, "owner-123")
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

}