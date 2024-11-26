package de.remsfal.service.boundary.project;


import de.remsfal.core.model.project.ChatMessageModel;
import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.core.model.project.TaskModel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import de.remsfal.service.TestData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ChatResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}";
    static final String CHAT_SESSION_TASK_PATH = BASE_PATH + "/tasks/{taskId}/chat";
    static final String CHAT_SESSION_DEFECT_PATH = BASE_PATH + "/defects/{defectId}/chat";
    static final String CHAT_SESSION_TASK_PATH_with_SESSION_ID = CHAT_SESSION_TASK_PATH + "/{sessionId}";

    static final String TASK_ID_1 = UUID.randomUUID().toString();
    static final String TASK_ID_2 = UUID.randomUUID().toString();
    static final String EXAMPLE_CHAT_SESSION_ID_1 = UUID.randomUUID().toString();
    static final String EXAMPLE_CHAT_SESSION_ID_2 = UUID.randomUUID().toString();
    static final String CHAT_MESSAGE_ID_1 = UUID.randomUUID().toString();
    static final String CHAT_MESSAGE_ID_2 = UUID.randomUUID().toString();
    static final String CHAT_MESSAGE_JSON_PAYLOAD = "{"
            + "\"chat_session_id\": \"" + EXAMPLE_CHAT_SESSION_ID_1 + "\","
            + "\"sender_id\": \"" + TestData.USER_ID + "\","
            + "\"contentType\": \"" + ChatMessageModel.ContentType.TEXT.name() + "\","
            + "\"content\": \"Test Message\""
            + "}";
    static final String CHAT_MESSAGE_JSON_PAYLOAD_BLANK_CONTENT = "{"
            + "\"chat_session_id\": \"" + EXAMPLE_CHAT_SESSION_ID_1 + "\","
            + "\"sender_id\": \"" + TestData.USER_ID + "\","
            + "\"contentType\": \"" + ChatMessageModel.ContentType.TEXT.name() + "\","
            + "\"content\": \"\""
            + "}";
    static final String CHAT_MESSAGE_JSON_PAYLOAD_NULL_CONTENT = "{"
            + "\"chat_session_id\": \"" + EXAMPLE_CHAT_SESSION_ID_1 + "\","
            + "\"sender_id\": \"" + TestData.USER_ID + "\","
            + "\"contentType\": \"" + ChatMessageModel.ContentType.TEXT.name() + "\","
            + "\"content\": null"
            + "}";




    @BeforeEach
    protected void setup() {

        // setup test users and projects , userId is the manager of all projects
        super.setupTestUsers();
        super.setupTestProjects();

        // setup  user roles
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
                .setParameter(1, TestData.PROJECT_ID_1)
                .setParameter(2, TestData.USER_ID_2)
                .setParameter(3, "CARETAKER")
                .executeUpdate());

        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
                .setParameter(1, TestData.PROJECT_ID_1)
                .setParameter(2, TestData.USER_ID_3)
                .setParameter(3, "LESSOR")
                .executeUpdate());

        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
                .setParameter(1, TestData.PROJECT_ID_1)
                .setParameter(2, TestData.USER_ID_4)
                .setParameter(3, "PROPRIETOR")
                .executeUpdate());

        // set up a task and a defect in project 1
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, DESCRIPTION, STATUS, CREATED_BY)"
                        +
                        " VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, TASK_ID_1)
                .setParameter(2, "TASK")
                .setParameter(3, TestData.PROJECT_ID)
                .setParameter(4, TestData.TASK_TITLE_1)
                .setParameter(5, TestData.TASK_DESCRIPTION_1)
                .setParameter(6, TaskModel.Status.OPEN.name())
                .setParameter(7, TestData.USER_ID)
                .executeUpdate());

        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, DESCRIPTION, STATUS, CREATED_BY)"
                        +
                        " VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, TASK_ID_2)
                .setParameter(2, "DEFECT")
                .setParameter(3, TestData.PROJECT_ID)
                .setParameter(4, "DEFECT TITLE")
                .setParameter(5, "DEFECT DESCRIPTION")
                .setParameter(6, TaskModel.Status.OPEN.name())
                .setParameter(7, TestData.USER_ID)
                .executeUpdate());

        // set up example chat session for a task in project 1
        runInTransaction(() -> entityManager.createNativeQuery(
                "INSERT INTO CHAT_SESSION (ID, PROJECT_ID, TASK_ID, TASK_TYPE, STATUS) VALUES (?,?,?,?,?)")
                .setParameter(1, EXAMPLE_CHAT_SESSION_ID_1)
                .setParameter(2, TestData.PROJECT_ID)
                .setParameter(3, TASK_ID_1)
                .setParameter(4, "TASK")
                .setParameter(5, "OPEN")
                .executeUpdate());

        // set up example chat session for a defect in project 1
        runInTransaction(() -> entityManager.createNativeQuery(
                "INSERT INTO CHAT_SESSION (ID, PROJECT_ID, TASK_ID, TASK_TYPE, STATUS) VALUES (?,?,?,?,?)")
                .setParameter(1, EXAMPLE_CHAT_SESSION_ID_2)
                .setParameter(2, TestData.PROJECT_ID)
                .setParameter(3, TASK_ID_2)
                .setParameter(4, "DEFECT")
                .setParameter(5, "OPEN")
                .executeUpdate());

        // set user-4 as initiator of the chat sessions
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO CHAT_SESSION_PARTICIPANT (CHAT_SESSION_ID, PARTICIPANT_ID , ROLE)" +
                        " VALUES (?,?,?)")
                .setParameter(1, EXAMPLE_CHAT_SESSION_ID_1)
                .setParameter(2, TestData.USER_ID_4)
                .setParameter(3, "INITIATOR")
                .executeUpdate());

        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO CHAT_SESSION_PARTICIPANT (CHAT_SESSION_ID, PARTICIPANT_ID , ROLE) " +
                        "VALUES (?,?,?)")
                .setParameter(1, EXAMPLE_CHAT_SESSION_ID_2)
                .setParameter(2, TestData.USER_ID_4)
                .setParameter(3, "INITIATOR")
                .executeUpdate());

        // set user-3 as participant of the chat sessions
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO CHAT_SESSION_PARTICIPANT (CHAT_SESSION_ID, PARTICIPANT_ID , ROLE) " +
                        "VALUES (?,?,?)")
                .setParameter(1, EXAMPLE_CHAT_SESSION_ID_1)
                .setParameter(2, TestData.USER_ID_3)
                .setParameter(3, "HANDLER")
                .executeUpdate());


        // insert hello world message in both sessions for testing
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO CHAT_MESSAGE (ID, CHAT_SESSION_ID, SENDER_ID, CONTENT_TYPE, CONTENT)"
                        +
                        " VALUES (?,?,?,?,?)")
                .setParameter(1, CHAT_MESSAGE_ID_1)
                .setParameter(2, EXAMPLE_CHAT_SESSION_ID_1)
                .setParameter(3, TestData.USER_ID_3)
                .setParameter(4, ChatMessageModel.ContentType.TEXT.name())
                .setParameter(5, "Hello World")
                .executeUpdate());

        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO CHAT_MESSAGE (ID, CHAT_SESSION_ID, SENDER_ID, CONTENT_TYPE, CONTENT)"
                        +
                        " VALUES (?,?,?,?,?)")
                .setParameter(1, CHAT_MESSAGE_ID_2)
                .setParameter(2, EXAMPLE_CHAT_SESSION_ID_2)
                .setParameter(3, TestData.USER_ID_4)
                .setParameter(4, ChatMessageModel.ContentType.TEXT.name())
                .setParameter(5, "Hello World")
                .executeUpdate());

    }

    @AfterEach
    void cleanUpChatMessages() {
        runInTransaction(() -> entityManager
                .createNativeQuery("DELETE FROM CHAT_MESSAGE WHERE CHAT_SESSION_ID = ?")
                .setParameter(1, EXAMPLE_CHAT_SESSION_ID_1)
                .executeUpdate());

        runInTransaction(() -> entityManager
                .createNativeQuery("DELETE FROM CHAT_MESSAGE WHERE CHAT_SESSION_ID = ?")
                .setParameter(1, EXAMPLE_CHAT_SESSION_ID_2)
                .executeUpdate());
    }

    // authentication test - same logic in all methods - no need for multiple tests
    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID})
    void getChatSession_UNAUTHENTICATED(String path) {
        given()
                .when()
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID})
    void getChatSession_UNPRIVILEGED(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID})
    void deleteChatSession_INVALID_INPUT(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .delete(path.replace("{projectId}", UUID.randomUUID().toString()).replace("{taskId}",
                                TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());


        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .delete(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}",
                                TASK_ID_1)
                        .replace("{sessionId}", UUID.randomUUID().toString()))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }



    // unprivileged tests - same logic in all methods - no need for multiple tests
    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants/{participantId}"})
    void changeParticipantRole_UNPRIVILEGED(String path)
    {
        String newRole = ChatSessionModel.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
                .body(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .put(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{participantId}", TestData.USER_ID_4))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    // validation tests
    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH})
    void createChatSession_FAILURE_INVALID_TASK(String path) {
        // invalid input - non-existing taskId in the database
        String nonExistingTaskId = UUID.randomUUID().toString();
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .post(path, TestData.PROJECT_ID_1, nonExistingTaskId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }


    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID})
    void getChatSession_FAILURE_INVALID_SESSION(String path) {
        // invalid input - non-existing sessionId in the database
        String nonExistingSessionId = UUID.randomUUID().toString();
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", nonExistingSessionId))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/status"})
    void updateChatSessionStatus_FAILURE_INVALID_STATUS(String path) {
        String invalidStatus = "INVALID_STATUS";
        String jsonBody = "\"" + invalidStatus + "\"";

        given()
                .body(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .put(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/status"})
    void updateChatSessionStatus_INVALID_SESSION(String path) {

        String newStatus = ChatSessionModel.Status.CLOSED.toString(); // "CLOSED"
        // Wrap the status in quotes to form a valid JSON string
        String jsonBody = "\"" + newStatus + "\"";

        given()
                .body(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .put(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", UUID.randomUUID().toString()))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }


    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants/{participantId}"})
    void getParticipant_FAILURE_INVALID_PARTICIPANT(String path) {
        String nonExistingParticipantId = UUID.randomUUID().toString();
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{participantId}", nonExistingParticipantId))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants/{participantId}"})
    void getParticipant_FAILURE_UNPRIVILEGED(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{participantId}",TestData.USER_ID_4 ))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants/{participantId}/role"})
    void changeParticipantRole_FAILURE_INVALID_ROLE(String path) {
        // invalid input - non-existing role in the database
        String nonExistingRole = "INVALID_ROLE";
        String jsonBody = "\"" + nonExistingRole + "\"";
        given()
                .body(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .put(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{participantId}", TestData.USER_ID_3))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants/{participantId}"})
    void changeParticipantRole_INVALID_SESSION(String path)
    {
        String newRole = ChatSessionModel.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
                .body(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .put(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", UUID.randomUUID().toString())
                        .replace("{participantId}", TestData.USER_ID_4))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());


    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID})
    void joinChatSession_INVALID_INPUT(String path)
    {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .post(path, TestData.PROJECT_ID_1, TASK_ID_1, UUID.randomUUID().toString())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    // important logic tests

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages"})
    void sendMessage_FAILURE_INVALID_PAYLOAD(String path) {
            String largePayload = "{\"content\":\"" + "a".repeat(9000) + "\"}";
            given()
                    .body(largePayload)
                    .contentType(ContentType.JSON)
                    .when()
                    .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                    .post(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants"})
    void getParticipants_EMPTY_SESSION(String path) {
        // Create an empty session
        String emptySessionId = "EMPTY_SESSION_ID";

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", emptySessionId))
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants"})
    void getParticipants_INVALID_INPUT(String path)
    {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_2)
                        .replace("{sessionId}", UUID.randomUUID().toString()))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants/{participantId}"})
    void removeParticipant_INVALID_INPUT(String path)
    {   String path_task1_session1 = path.replace("{projectId}", TestData.PROJECT_ID_1)
            .replace("{taskId}", TASK_ID_1)
            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .delete(path_task1_session1
                        .replace("{participantId}", UUID.randomUUID().toString()))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .delete(path_task1_session1
                        .replace("{participantId}", TestData.USER_ID_3))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages"})
    void sendMessage_INVALID_INPUT(String path) {
        given()
                .body(CHAT_MESSAGE_JSON_PAYLOAD_BLANK_CONTENT)
                .contentType(ContentType.JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
                .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given()
                .body(CHAT_MESSAGE_JSON_PAYLOAD_NULL_CONTENT)
                .contentType(ContentType.JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
                .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }


    // success tests
    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_DEFECT_PATH})
    void createChatSession_OnDefect_SUCCESS(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .post(path, TestData.PROJECT_ID_1, TASK_ID_2)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(ContentType.JSON)
                .header("Location", Matchers.containsString(path.replace("{projectId}",
                        TestData.PROJECT_ID_1).replace("{defectId}", TASK_ID_2)))
                .and().body("id", notNullValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH})
    void createChatSession_OnTask_SUCCESS(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .post(path, TestData.PROJECT_ID_1, TASK_ID_1)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(ContentType.JSON)
                .header("Location", Matchers.containsString(path.replace("{projectId}",
                        TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)))
                .and().body("id", notNullValue());
    }


    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID})
    void getChatSession_SUCCESS(String path)
    {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", equalTo(EXAMPLE_CHAT_SESSION_ID_1));


    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID})
    void deleteChatSession_SUCCESS(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .delete(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }


    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/status"})
    void updateChatSessionStatus_SUCCESS(String path) {

        String newStatus = ChatSessionModel.Status.CLOSED.toString(); // "CLOSED"
        // Wrap the status in quotes to form a valid JSON string
        String jsonBody = "\"" + newStatus + "\"";

        given()
                .body(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .put(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("status", equalTo(newStatus));
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID})
    void joinChatSession_SUCCESS(String path)
    {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("[0].userId", equalTo(TestData.USER_ID_4))
                .body("[0].userRole", equalTo("INITIATOR"))
                .body("[1].userId", equalTo(TestData.USER_ID_3))
                .body("[1].userRole", equalTo("HANDLER"))
                .body("[2].userId", equalTo(TestData.USER_ID))
                .body("[2].userRole", equalTo("OBSERVER"));
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants"})
    void getParticipants_SUCCESS(String path)
    {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_2)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_2))
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("[0].userId", equalTo(TestData.USER_ID_4))
                .body("[0].userRole", equalTo("INITIATOR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants/{participantId}"})
    void getParticipant_SUCCESS(String path)
    {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{participantId}", TestData.USER_ID_4))
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("[0].userId", equalTo(TestData.USER_ID_4))
                .body("[0].userRole", equalTo("INITIATOR"));

    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants/{participantId}"})
    void changeParticipantRole_SUCCESS(String path)
    {
        String newRole = ChatSessionModel.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
                .body(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .put(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{participantId}", TestData.USER_ID_4))
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("[0].userId", equalTo(TestData.USER_ID_4))
                .body("[0].userRole", equalTo(newRole));


    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/participants/{participantId}"})
    void removeParticipant_SUCCESS(String path)
    {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .delete(path.replace("{projectId}", TestData.PROJECT_ID_1)
                        .replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{participantId}", TestData.USER_ID_3))
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages"})
    void sendMessage_SUCCESS(String path) {
        given()
                .body(CHAT_MESSAGE_JSON_PAYLOAD)
                .contentType(ContentType.JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
                .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(ContentType.JSON)
                .body("id", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages"})
    void getChatMessages_SUCCESS(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("CHAT_SESSION_ID", equalTo(EXAMPLE_CHAT_SESSION_ID_1))
                .body("TASK_ID", equalTo(TASK_ID_1))
                .body("PROJECT_ID", equalTo(TestData.PROJECT_ID_1))
                .body("TASK_TYPE", equalTo("TASK"))
                .body("messages[0].DATETIME", notNullValue())
                .body("messages[0].MESSAGE_ID", notNullValue())
                .body("messages[0].SENDER_ID", equalTo(TestData.USER_ID_3))
                .body("messages[0].MEMBER_ROLE", equalTo("HANDLER"))
                .body("messages[0].MESSAGE_TYPE", equalTo("TEXT"))
                .body("messages[0].MESSAGE_CONTENT", equalTo("Hello World"));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages/{messageId}"})
    void getChatMessage_SUCCESS(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{messageId}", CHAT_MESSAGE_ID_1))
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("id", equalTo(CHAT_MESSAGE_ID_1))
                .body("chatSessionId", equalTo(EXAMPLE_CHAT_SESSION_ID_1))
                .body("senderId", equalTo(TestData.USER_ID_3))
                .body("contentType", equalTo("TEXT"))
                .body("content", equalTo("Hello World"))
                .body("imageUrl", nullValue())
                .body("timestamp", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages"})
    void getChatMessages_FAILURE(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", UUID.randomUUID().toString()))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .get(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages/{messageId}"})
    void getChatMessage_FAILURE(String path) {
        String path_project1_session1 = path.replace("{projectId}", TestData.PROJECT_ID_1)
                .replace("{taskId}", TASK_ID_1)
                .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(path_project1_session1
                        .replace("{messageId}", UUID.randomUUID().toString()))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .get(path_project1_session1
                        .replace("{messageId}", CHAT_MESSAGE_ID_1))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages/{messageId}"})
    void updateChatMessage_SUCCESS(String path) {
        String updatedMessageJson = "{\"content\":\"Updated Hello World\"}";

        given()
                .body(updatedMessageJson)
                .contentType(ContentType.JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .put(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{messageId}", CHAT_MESSAGE_ID_1))
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("id", equalTo(CHAT_MESSAGE_ID_1))
                .body("chatSessionId", equalTo(EXAMPLE_CHAT_SESSION_ID_1))
                .body("senderId", equalTo(TestData.USER_ID_3))
                .body("contentType", equalTo("TEXT"))
                .body("content", equalTo("Updated Hello World"))
                .body("imageUrl", nullValue())
                .body("timestamp", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages/{messageId}"})
    void updateChatMessage_FAILURE(String path) {
        String path_project1_session1 = path.replace("{projectId}", TestData.PROJECT_ID_1)
                .replace("{taskId}", TASK_ID_1)
                .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        String updatedMessageJsonBlank = "{\"content\":\"\"}";
        String updatedMessageJson = "{\"content\":\"Updated Hello World\"}";
        String put_request = path_project1_session1
                .replace("{messageId}", CHAT_MESSAGE_ID_1);


        given()
                .body(updatedMessageJsonBlank)
                .contentType(ContentType.JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .put(put_request)
                .then()
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .contentType(ContentType.JSON);

        given()
                .body(updatedMessageJson)
                .contentType(ContentType.JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .put(put_request)
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
                .body(updatedMessageJson)
                .contentType(ContentType.JSON)
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .put(path_project1_session1
                        .replace("{messageId}", UUID.randomUUID().toString()))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages/{messageId}"})
    void deleteChatMessage_SUCCESS(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .delete(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
                        .replace("{messageId}", CHAT_MESSAGE_ID_1))
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_with_SESSION_ID + "/messages/{messageId}"})
    void deleteChatMessage_FAILURE(String path) {
        String path_project1_session1 = path.replace("{projectId}", TestData.PROJECT_ID_1)
                .replace("{taskId}", TASK_ID_1)
                .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .delete(path_project1_session1
                        .replace("{messageId}", UUID.randomUUID().toString()))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
                .delete(path_project1_session1
                        .replace("{messageId}", CHAT_MESSAGE_ID_1))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .delete(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                        .replace("{sessionId}", UUID.randomUUID().toString())
                        .replace("{messageId}", CHAT_MESSAGE_ID_1))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

}
