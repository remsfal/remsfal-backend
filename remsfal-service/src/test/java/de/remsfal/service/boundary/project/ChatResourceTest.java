package de.remsfal.service.boundary.project;


import de.remsfal.core.model.project.ChatMessageModel;
import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.core.model.project.TaskModel;
import de.remsfal.service.TestData;
import de.remsfal.service.control.ChatMessageController;
import de.remsfal.service.control.FileStorageService;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class ChatResourceTest extends AbstractProjectResourceTest {

    @Inject
    FileStorageService fileStorageService;

    @Inject
    ChatMessageController chatMessageController;

    static final String BASE_PATH = "/api/v1/projects/{projectId}";
    static final String CHAT_SESSION_TASK_PATH = BASE_PATH + "/tasks/{taskId}/chats";
    static final String CHAT_SESSION_DEFECT_PATH = BASE_PATH + "/defects/{defectId}/chats";
    static final String CHAT_SESSION_TASK_PATH_WITH_SESSION_ID = CHAT_SESSION_TASK_PATH + "/{sessionId}";

    static final String TASK_ID_1 = UUID.randomUUID().toString();
    static final String TASK_ID_2 = UUID.randomUUID().toString();
    static final String EXAMPLE_CHAT_SESSION_ID_1 = UUID.randomUUID().toString();
    static final String EXAMPLE_CHAT_SESSION_ID_2 = UUID.randomUUID().toString();
    static final String EXAMPLE_FILE_NAME_PREFIX = "example";
    static final String EXAMPLE_FILE_NAME_SUFFIX = "txt";
    static final String EXAMPLE_FILE_NAME = EXAMPLE_FILE_NAME_PREFIX + "." + EXAMPLE_FILE_NAME_SUFFIX;

    static final String CHAT_MESSAGE_ID_1 = UUID.randomUUID().toString();
    static final String CHAT_MESSAGE_ID_2 = UUID.randomUUID().toString();
    static final String CHAT_MESSAGE_ID_3 = UUID.randomUUID().toString();
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

    private final String bucketName = "remsfal-chat-files";


    @BeforeEach
    protected void setup() throws Exception {

        // setup test users and projects , userId is the manager of all projects
        super.setupTestUsers();
        super.setupTestProjects();

        // setup  user roles
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID_1)
            .setParameter(2, TestData.USER_ID_2)
            .setParameter(3, "STAFF")
            .executeUpdate());

        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID_1)
            .setParameter(2, TestData.USER_ID_3)
            .setParameter(3, "LESSOR")
            .executeUpdate());

        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
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

        // create a file
        Path tempDir = Files.createTempDirectory("test-");
        Path tempFile = tempDir.resolve(EXAMPLE_FILE_NAME);
        Files.writeString(tempFile, "Hello World");
        String exampleFileName = tempFile.getFileName().toString();
        MultipartFormDataInput exampleFile =
            createMultipartFormDataInput(exampleFileName, MediaType.TEXT_PLAIN, Files.readAllBytes(tempFile));
        String exampleFileUrl = fileStorageService.uploadFile(bucketName, exampleFile);
        // insert the metadata to CHAT_MESSAGE table and upload the file to minio bucket
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO CHAT_MESSAGE (ID, CHAT_SESSION_ID, SENDER_ID, CONTENT_TYPE, URL) "
                +
                "VALUES (?,?,?,?,?)")
            .setParameter(1, CHAT_MESSAGE_ID_3)
            .setParameter(2, EXAMPLE_CHAT_SESSION_ID_1)
            .setParameter(3, TestData.USER_ID_3)
            .setParameter(4, ChatMessageModel.ContentType.FILE.name())
            .setParameter(5, exampleFileUrl)
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

        fileStorageService.deleteObject(bucketName, EXAMPLE_FILE_NAME);
    }

    // authentication test - same logic in all methods - no need for multiple tests
    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID})
    void getChatSession_UNAUTHENTICATED(String path) {
        given()
            .when()
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID})
    void getChatSession_UNPRIVILEGED(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID})
    void deleteChatSession_INVALID_INPUT(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path, UUID.randomUUID().toString(), TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());


        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path, TestData.PROJECT_ID_1, TASK_ID_1, UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }


    // unprivileged tests - same logic in all methods - no need for multiple tests
    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}"})
    void changeParticipantRole_UNPRIVILEGED(String path) {
        String newRole = ChatSessionModel.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .put(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TestData.USER_ID_4)
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
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .post(path, TestData.PROJECT_ID_1, nonExistingTaskId)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }


    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID})
    void getChatSession_FAILURE_INVALID_SESSION(String path) {
        // invalid input - non-existing sessionId in the database
        String nonExistingSessionId = UUID.randomUUID().toString();
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, nonExistingSessionId)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/status"})
    void updateChatSessionStatus_FAILURE_INVALID_STATUS(String path) {
        String invalidStatus = "INVALID_STATUS";
        String jsonBody = "\"" + invalidStatus + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/status"})
    void updateChatSessionStatus_INVALID_SESSION(String path) {

        String newStatus = ChatSessionModel.Status.CLOSED.toString(); // "CLOSED"
        // Wrap the status in quotes to form a valid JSON string
        String jsonBody = "\"" + newStatus + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TestData.PROJECT_ID_1, TASK_ID_1, UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }


    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}"})
    void getParticipant_FAILURE_INVALID_PARTICIPANT(String path) {
        String nonExistingParticipantId = UUID.randomUUID().toString();
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, nonExistingParticipantId)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}"})
    void getParticipant_FAILURE_UNPRIVILEGED(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}/role"})
    void changeParticipantRole_FAILURE_INVALID_ROLE(String path) {
        // invalid input - non-existing role in the database
        String nonExistingRole = "INVALID_ROLE";
        String jsonBody = "\"" + nonExistingRole + "\"";
        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TestData.USER_ID_3)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}"})
    void changeParticipantRole_INVALID_SESSION(String path) {
        String newRole = ChatSessionModel.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TestData.PROJECT_ID_1, TASK_ID_1, UUID.randomUUID().toString(), TestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());


    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID})
    void joinChatSession_INVALID_INPUT(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .post(path, TestData.PROJECT_ID_1, TASK_ID_1, UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    // important logic tests

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages"})
    void sendMessage_FAILURE_INVALID_PAYLOAD(String path) {
        String largePayload = "{\"content\":\"" + "a".repeat(9000) + "\"}";
        given()
            .body(largePayload)
            .contentType(ContentType.JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants"})
    void getParticipants_EMPTY_SESSION(String path) {
        // Create an empty session
        String emptySessionId = "EMPTY_SESSION_ID";

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, emptySessionId)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants"})
    void getParticipants_INVALID_INPUT(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .get(path, TestData.PROJECT_ID_1, TASK_ID_2, UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}"})
    void removeParticipant_INVALID_INPUT(String path) {
        String path_task1_session1 = path.replace("{projectId}", TestData.PROJECT_ID_1)
            .replace("{taskId}", TASK_ID_1)
            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .delete(path_task1_session1
                .replace("{participantId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .delete(path_task1_session1
                .replace("{participantId}", TestData.USER_ID_3))
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages"})
    void sendMessage_INVALID_INPUT(String path) {
        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD_BLANK_CONTENT)
            .contentType(ContentType.JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD_NULL_CONTENT)
            .contentType(ContentType.JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
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
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID})
    void getChatSession_SUCCESS(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", equalTo(EXAMPLE_CHAT_SESSION_ID_1));


    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID})
    void deleteChatSession_SUCCESS(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }


    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/status"})
    void updateChatSessionStatus_SUCCESS(String path) {

        String newStatus = ChatSessionModel.Status.CLOSED.toString(); // "CLOSED"
        // Wrap the status in quotes to form a valid JSON string
        String jsonBody = "\"" + newStatus + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("status", equalTo(newStatus));
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID})
    void joinChatSession_SUCCESS(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants"})
    void getParticipants_SUCCESS(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .get(path, TestData.PROJECT_ID_1, TASK_ID_2, EXAMPLE_CHAT_SESSION_ID_2)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].userId", equalTo(TestData.USER_ID_4))
            .body("[0].userRole", equalTo("INITIATOR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}"})
    void getParticipant_SUCCESS(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].userId", equalTo(TestData.USER_ID_4))
            .body("[0].userRole", equalTo("INITIATOR"));

    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}"})
    void changeParticipantRole_SUCCESS(String path) {
        String newRole = ChatSessionModel.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].userId", equalTo(TestData.USER_ID_4))
            .body("[0].userRole", equalTo(newRole));


    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}"})
    void removeParticipant_SUCCESS(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .delete(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TestData.USER_ID_3)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages"})
    void sendMessage_SUCCESS(String path) {
        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD)
            .contentType(ContentType.JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .body("id", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages"})
    void getChatMessages_SUCCESS(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
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
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}"})
    void getChatMessage_SUCCESS(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, CHAT_MESSAGE_ID_1)
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
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}"})
    void getChatMessage_FILETYPE_SUCCESS(String path) {
        String resolvedPath = path
            .replace("{projectId}", TestData.PROJECT_ID_1)
            .replace("{taskId}", TASK_ID_1)
            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
            .replace("{messageId}", CHAT_MESSAGE_ID_3);

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(resolvedPath)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString("filename=\"" + EXAMPLE_FILE_NAME + "\""))
            .body(not(emptyString()));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages"})
    void getChatMessages_FAILURE(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}"})
    void getChatMessage_FAILURE(String path) {
        String path_project1_session1 = path.replace("{projectId}", TestData.PROJECT_ID_1)
            .replace("{taskId}", TASK_ID_1)
            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path_project1_session1
                .replace("{messageId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(path_project1_session1
                .replace("{messageId}", CHAT_MESSAGE_ID_1))
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}"})
    void updateChatMessage_SUCCESS(String path) {
        String updatedMessageJson = "{\"content\":\"Updated Hello World\"}";

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, CHAT_MESSAGE_ID_1)
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
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}"})
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
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(put_request)
            .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .contentType(ContentType.JSON);

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .put(put_request)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path_project1_session1
                .replace("{messageId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}"})
    void deleteChatMessage_SUCCESS(String path) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}"})
    void deleteChatMessage_FAILURE(String path) {
        String path_project1_session1 = path.replace("{projectId}", TestData.PROJECT_ID_1)
            .replace("{taskId}", TASK_ID_1)
            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path_project1_session1
                .replace("{messageId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .delete(path_project1_session1
                .replace("{messageId}", CHAT_MESSAGE_ID_1))
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path.replace("{projectId}", TestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                .replace("{sessionId}", UUID.randomUUID().toString())
                .replace("{messageId}", CHAT_MESSAGE_ID_1))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }


    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload"})
    void uploadFile_SUCCESS(String path) throws Exception {
        // Create a temporary directory and file
        Path tempDir = Files.createTempDirectory("test-upload");
        Path tempFile = tempDir.resolve("test-file.txt");
        Files.writeString(tempFile, "This is a test file content");
        String fileName = tempFile.getFileName().toString();
        String expectedBucketName = "remsfal-chat-files";

        try {
            // Perform the file upload and extract the fileId and fileUrl from the response
            Map<String, String> response =
                given()
                    .multiPart("file", tempFile.toFile(), MediaType.TEXT_PLAIN)
                    .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                    .when()
                    .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                    .then()
                    .statusCode(Response.Status.CREATED.getStatusCode())
                    .contentType(ContentType.JSON)
                    .extract().as(new TypeRef<>() {
                    });

            String fileId = response.get("fileId");
            String fileUrl = response.get("fileUrl");

            // Verify the file metadata is persisted using ChatMessageController
            ChatMessageEntity persistedMessage = chatMessageController.getChatMessage(fileId);

            assertNotNull(persistedMessage, "Persisted message should not be null");
            assertEquals(EXAMPLE_CHAT_SESSION_ID_1, persistedMessage.getChatSession().getId(),
                "Chat session ID should match");
            assertEquals(ChatMessageModel.ContentType.FILE, persistedMessage.getContentType(),
                "Content type should match");
            assertEquals(fileUrl, persistedMessage.getUrl(), "Persisted URL should match the returned file URL");
            // Verify the file is stored in the MinIO bucket
            verifyFileInBucket(expectedBucketName, fileName);

        } finally {
            // Clean up the temporary file and directory
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }
    }


    private void verifyFileInBucket(String bucketName, String fileName) {
        boolean fileExists = false;
        try {
            Iterable<Result<Item>> items = fileStorageService.listObjects(bucketName);
            for (Result<Item> item : items) {
                if (item.get().objectName().contains(fileName)) {
                    fileExists = true;
                    break;
                }
            }
        } catch (Exception e) {
            fail("Exception occurred while verifying the file in the bucket: " + e.getMessage(), e);
        }
        assertTrue(fileExists, "Uploaded file should exist in the bucket");
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload"})
    void uploadFile_MissingFilePart_FAILURE(String path) {
        given()
            .multiPart("someField", "someValue") // This ensures a valid multipart request with a boundary
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .when()
            .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .body("message", equalTo("No file part found in the form data"));


    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload"})
    void uploadFile_InvalidContentType_FAILURE(String path) throws Exception {
        Path tempFile = Files.createTempFile("test-file", ".exe"); // Unsupported file type
        try {
            given()
                .multiPart("file", tempFile.toFile(), "application/x-msdownload")
                .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .when()
                .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode())
                .body("message", containsString("Unsupported Media Type: application/x-msdownload"));

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload"})
    void uploadFile_EmptyInputStream_FAILURE(String path) {
        given()
            .multiPart("file", "", MediaType.TEXT_PLAIN) // Empty file content
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .when()
            .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .body("message", equalTo("Failed to read file stream: unknown"));
    }

    @ParameterizedTest
    @ValueSource(strings = {CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload"})
    void uploadFile_ChatSessionClosed_FAILURE(String path) throws Exception {
        runInTransaction(() -> {
            entityManager.createQuery("UPDATE ChatSessionEntity SET status = :status WHERE id = :id")
                .setParameter("status", ChatSessionModel.Status.CLOSED)
                .setParameter("id", EXAMPLE_CHAT_SESSION_ID_1)
                .executeUpdate();
        });

        Path tempFile = Files.createTempFile("test-file", ".txt");
        try {
            given()
                .multiPart("file", tempFile.toFile(), MediaType.TEXT_PLAIN)
                .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .when()
                .post(path, TestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private MultipartFormDataInput createMultipartFormDataInput(String fileName, String contentType, byte[] content)
        throws Exception {
        MultipartFormDataInput input = mock(MultipartFormDataInput.class);
        InputPart inputPart = mock(InputPart.class);

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("Content-Disposition", "form-data; name=\"file\"; filename=\"" + fileName + "\"");

        when(inputPart.getHeaders()).thenReturn(headers);
        when(inputPart.getMediaType()).thenReturn(MediaType.valueOf(contentType));
        when(inputPart.getBody(InputStream.class, null)).thenReturn(new ByteArrayInputStream(content));
        when(input.getFormDataMap()).thenReturn(Map.of("file", List.of(inputPart)));

        return input;
    }
}
