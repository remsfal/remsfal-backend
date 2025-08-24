package de.remsfal.chat.boundary;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.chat.TicketingTestData;
import de.remsfal.chat.control.ChatMessageController;
import de.remsfal.chat.control.FileStorageController;
import de.remsfal.chat.entity.dao.ChatMessageRepository;
import de.remsfal.chat.entity.dao.ChatSessionRepository;
import de.remsfal.chat.entity.dao.FileStorage;
import de.remsfal.chat.entity.dao.ChatSessionRepository.ParticipantRole;
import de.remsfal.chat.entity.dto.ChatMessageEntity;
import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ChatSessionResourceTest extends AbstractResourceTest {

    @Inject
    FileStorageController fileStorageService;

    @Inject
    ChatMessageController chatMessageController;

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    CqlSession cqlSession;

    static final String BASE_PATH = "/api/v1/projects/{projectId}";
    static final String CHAT_SESSION_TASK_PATH = BASE_PATH + "/tasks/{taskId}/chats";
    static final String CHAT_SESSION_DEFECT_PATH = BASE_PATH + "/defects/{defectId}/chats";
    static final String CHAT_SESSION_TASK_PATH_WITH_SESSION_ID = CHAT_SESSION_TASK_PATH + "/{sessionId}";

    static final String TASK_ID_1 = "5b111b34-1073-4f48-a79d-f19b17e7d56b";
    static final String TASK_ID_2 = "4b8cd355-ad07-437a-9e71-a4e2e3624957";
    static final UUID TASK_ID_1_UUID = UUID.fromString(TASK_ID_1);
    static final UUID TASK_ID_2_UUID = UUID.fromString(TASK_ID_2);
    static final String EXAMPLE_CHAT_SESSION_ID_1 = "64ab9ef0-25ef-4a1c-81c9-5963f7c7d211";
    static final String EXAMPLE_CHAT_SESSION_ID_2 = "30444d17-56a9-4275-a9a8-e4fb7305359a";
    static final UUID EXAMPLE_CHAT_SESSION_ID_1_UUID = UUID.fromString(EXAMPLE_CHAT_SESSION_ID_1);
    static final UUID EXAMPLE_CHAT_SESSION_ID_2_UUID = UUID.fromString(EXAMPLE_CHAT_SESSION_ID_2);
    static final UUID PROJECT_ID_1_UUID = UUID.fromString(TicketingTestData.PROJECT_ID_1);
    static final UUID USER_ID_3_UUID = UUID.fromString(TicketingTestData.USER_ID_3);
    static final UUID USER_ID_4_UUID = UUID.fromString(TicketingTestData.USER_ID_4);

    static final String CHAT_MESSAGE_ID_1 = "b9854462-abb8-4213-8b15-be9290a19959";
    static final String CHAT_MESSAGE_ID_2 = "3f72a368-48bd-405e-976f-51a5c417a5c2";
    static final String CHAT_MESSAGE_ID_3 = "42817454-dc1e-476e-93d5-e073b424f191";
    static final UUID CHAT_MESSAGE_ID_1_UUID = UUID.fromString(CHAT_MESSAGE_ID_1);
    static final UUID CHAT_MESSAGE_ID_2_UUID = UUID.fromString(CHAT_MESSAGE_ID_2);
    static final UUID CHAT_MESSAGE_ID_3_UUID = UUID.fromString(CHAT_MESSAGE_ID_3);
    static final String CHAT_MESSAGE_JSON_PAYLOAD = "{"
        + "\"session_id\": \"" + EXAMPLE_CHAT_SESSION_ID_1 + "\","
        + "\"sender_id\": \"" + TicketingTestData.USER_ID + "\","
        + "\"contentType\": \"" + ContentType.TEXT.name() + "\","
        + "\"content\": \"Test Message\""
        + "}";
    static final String CHAT_MESSAGE_JSON_PAYLOAD_BLANK_CONTENT = "{"
        + "\"session_id\": \"" + EXAMPLE_CHAT_SESSION_ID_1 + "\","
        + "\"sender_id\": \"" + TicketingTestData.USER_ID + "\","
        + "\"contentType\": \"" + ContentType.TEXT.name() + "\","
        + "\"content\": \"\""
        + "}";
    static final String CHAT_MESSAGE_JSON_PAYLOAD_NULL_CONTENT = "{"
        + "\"session_id\": \"" + EXAMPLE_CHAT_SESSION_ID_1 + "\","
        + "\"sender_id\": \"" + TicketingTestData.USER_ID + "\","
        + "\"contentType\": \"" + ContentType.TEXT.name() + "\","
        + "\"content\": null"
        + "}";

    @BeforeEach
    protected void setup() throws Exception {
        logger.info("Setting up test data");
        super.setupTestUsers();
        super.setupTestMemberships();
        super.setupTestFiles();
        logger.info("Setting up chat sessions and messages");
        String insertChatSessionCql = "INSERT INTO remsfal.chat_sessions " +
            "(project_id, task_id, session_id, created_at, participants) " +
            "VALUES (?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatSessionCql,
            PROJECT_ID_1_UUID, TASK_ID_1_UUID, EXAMPLE_CHAT_SESSION_ID_1_UUID,
            Instant.now(),
            Map.of(
                USER_ID_4_UUID, ParticipantRole.INITIATOR.name(),
                USER_ID_3_UUID, ParticipantRole.HANDLER.name()));
        logger.info("Session 1 " + EXAMPLE_CHAT_SESSION_ID_1 +
            " created. " +
            "On project " + TicketingTestData.PROJECT_ID_1 + ": TASK_ID_1.");
        logger.info("Session 1 participants: " + TicketingTestData.USER_ID_4 + " as " + ParticipantRole.INITIATOR.name() +
            " and " + TicketingTestData.USER_ID_3 + " as " + ParticipantRole.HANDLER.name());
        String insertChatSessionCql2 = "INSERT INTO remsfal.chat_sessions " +
            "(project_id, task_id, session_id, created_at, participants) " +
            "VALUES (?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatSessionCql2,
            PROJECT_ID_1_UUID, TASK_ID_2_UUID, EXAMPLE_CHAT_SESSION_ID_2_UUID,
            Instant.now(),
            Map.of(
                USER_ID_4_UUID, ParticipantRole.INITIATOR.name(),
                USER_ID_3_UUID, ParticipantRole.HANDLER.name()));
        logger.info("Session 2 " + EXAMPLE_CHAT_SESSION_ID_2 +
            " created. " +
            "On project " + TicketingTestData.PROJECT_ID + ": TASK_ID_2.");
        logger.info("Session 2 participants: " + TicketingTestData.USER_ID_4 + " as " + ParticipantRole.INITIATOR.name() +
            " and " + TicketingTestData.USER_ID_3 + " as " + ParticipantRole.HANDLER.name());
        String insertChatMessageCql = "INSERT INTO remsfal.chat_messages " +
            "(session_id, message_id, sender_id, content_type, content, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatMessageCql,
            EXAMPLE_CHAT_SESSION_ID_1_UUID, CHAT_MESSAGE_ID_1_UUID, USER_ID_3_UUID,
            ChatMessageRepository.ContentType.TEXT.name(), "Hello World", Instant.now());

        logger.info("Message 1 " + CHAT_MESSAGE_ID_1 +
            " created. " +
            "On session " + EXAMPLE_CHAT_SESSION_ID_1 + " by " + TicketingTestData.USER_ID_3);

        String insertChatMessageCql2 = "INSERT INTO remsfal.chat_messages " +
            "(session_id, message_id, sender_id, content_type, content, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatMessageCql2,
            EXAMPLE_CHAT_SESSION_ID_2_UUID, CHAT_MESSAGE_ID_2_UUID, USER_ID_4_UUID,
            ChatMessageRepository.ContentType.TEXT.name(), "Hello World", Instant.now());
        logger.info("Message 2 " + CHAT_MESSAGE_ID_2 +
            " created. " +
            "On session " + EXAMPLE_CHAT_SESSION_ID_2 + " by " + TicketingTestData.USER_ID_4);
        logger.info("Setting up example file for message 3");
        String insertChatMessageCql3 = "INSERT INTO remsfal.chat_messages " +
            "(session_id, message_id, sender_id, content_type, url, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatMessageCql3,
            EXAMPLE_CHAT_SESSION_ID_1_UUID, CHAT_MESSAGE_ID_3_UUID, USER_ID_3_UUID,
            ChatMessageRepository.ContentType.FILE.name(), TicketingTestData.FILE_PNG_PATH, Instant.now());
        logger.info("Message 3 " + CHAT_MESSAGE_ID_3 +
            " created. " +
            "On session " + EXAMPLE_CHAT_SESSION_ID_1 + " by " + TicketingTestData.USER_ID_3);
    }

    // authentication test - same logic in all methods - no need for multiple tests
    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID })
    void getChatSession_UNAUTHENTICATED(String path) {
        given()
            .when()
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID })
    void getChatSession_UNPRIVILEGED(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID })
    void deleteChatSession_INVALID_INPUT(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path, UUID.randomUUID().toString(), TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    // unprivileged tests - same logic in all methods - no need for multiple tests
    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}" })
    void changeParticipantRole_UNPRIVILEGED(String path) {
        String newRole = ChatSessionRepository.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .put(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TicketingTestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID })
    void getChatSession_FAILURE_INVALID_SESSION(String path) {
        // invalid input - non-existing sessionId in the database
        String nonExistingSessionId = UUID.randomUUID().toString();
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, nonExistingSessionId)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}" })
    void getParticipant_FAILURE_INVALID_PARTICIPANT(String path) {
        String nonExistingParticipantId = UUID.randomUUID().toString();
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, nonExistingParticipantId)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}" })
    void getParticipant_FAILURE_UNPRIVILEGED(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TicketingTestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}/role" })
    void changeParticipantRole_FAILURE_INVALID_ROLE(String path) {
        // invalid input - non-existing role in the database
        String nonExistingRole = "INVALID_ROLE";
        String jsonBody = "\"" + nonExistingRole + "\"";
        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TicketingTestData.USER_ID_3)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID })
    void joinChatSession_INVALID_INPUT(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    // important logic tests

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages" })
    void sendMessage_FAILURE_INVALID_PAYLOAD(String path) {
        String largePayload = "{\"content\":\"" + "a".repeat(9000) + "\"}";
        given()
            .body(largePayload)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants" })
    void getParticipants_EMPTY_SESSION(String path) {
        // Create an empty session
        String emptySessionId = "EMPTY_SESSION_ID";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, emptySessionId)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}" })
    void removeParticipant_INVALID_INPUT(String path) {
        String path_task1_session1 = path.replace("{projectId}", TicketingTestData.PROJECT_ID_1)
            .replace("{taskId}", TASK_ID_1)
            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .delete(path_task1_session1
                .replace("{participantId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .delete(path_task1_session1
                .replace("{participantId}", TicketingTestData.USER_ID_3))
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages" })
    void sendMessage_INVALID_INPUT(String path) {
        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD_BLANK_CONTENT)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_4, TicketingTestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD_NULL_CONTENT)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_4, TicketingTestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH })
    void createChatSession_OnTask_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("Location", Matchers.containsString(path.replace("{projectId}",
                TicketingTestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)))
            .and().body("sessionId", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID })
    void getChatSession_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("sessionId", equalTo(EXAMPLE_CHAT_SESSION_ID_1));

    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID })
    void deleteChatSession_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID })
    void joinChatSession_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[2].userId", equalTo(TicketingTestData.USER_ID_4))
            .body("[2].userRole", equalTo("INITIATOR"))
            .body("[1].userId", equalTo(TicketingTestData.USER_ID_3))
            .body("[1].userRole", equalTo("HANDLER"))
            .body("[0].userId", equalTo(TicketingTestData.USER_ID))
            .body("[0].userRole", equalTo("OBSERVER"));
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants" })
    void getParticipants_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_2, EXAMPLE_CHAT_SESSION_ID_2)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[1].userId", equalTo(TicketingTestData.USER_ID_4))
            .body("[1].userRole", equalTo("INITIATOR"));
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}" })
    void getParticipant_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TicketingTestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].userId", equalTo(TicketingTestData.USER_ID_4))
            .body("[0].userRole", equalTo("INITIATOR"));

    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}" })
    void changeParticipantRole_SUCCESS(String path) {
        String newRole = ChatSessionRepository.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TicketingTestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].userId", equalTo(TicketingTestData.USER_ID_4))
            .body("[0].userRole", equalTo(newRole));

    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/participants/{participantId}" })
    void removeParticipant_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .delete(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, TicketingTestData.USER_ID_3)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages" })
    void sendMessage_SUCCESS(String path) {
        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_4, TicketingTestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .body("messageId", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages" })
    void getChatMessages_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("SESSION_ID", equalTo(EXAMPLE_CHAT_SESSION_ID_1))
            .body("TASK_ID", equalTo(TASK_ID_1))
            .body("PROJECT_ID", equalTo(TicketingTestData.PROJECT_ID_1))
            .body("messages[1].DATETIME", notNullValue())
            .body("messages[1].message_id", notNullValue())
            .body("messages[1].SENDER_ID", equalTo(TicketingTestData.USER_ID_3))
            .body("messages[1].MEMBER_ROLE", equalTo("HANDLER"))
            .body("messages[1].MESSAGE_TYPE", equalTo("TEXT"))
            .body("messages[1].MESSAGE_CONTENT", equalTo("Hello World"));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}" })
    void getChatMessage_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("messageId", equalTo(CHAT_MESSAGE_ID_1))
            .body("sessionId", equalTo(EXAMPLE_CHAT_SESSION_ID_1))
            .body("senderId", equalTo(TicketingTestData.USER_ID_3))
            .body("contentType", equalTo("TEXT"))
            .body("content", equalTo("Hello World"))
            .body("url", nullValue())
            .body("createdAt", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}" })
    void getChatMessage_FILETYPE_SUCCESS(String path) {
        String resolvedPath = path
            .replace("{projectId}", TicketingTestData.PROJECT_ID_1)
            .replace("{taskId}", TASK_ID_1)
            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1)
            .replace("{messageId}", CHAT_MESSAGE_ID_3);

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(resolvedPath)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString("filename=\"" + TicketingTestData.FILE_PNG_PATH + "\""))
            .body(not(emptyString()));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages" })
    void getChatMessages_FAILURE(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}" })
    void getChatMessage_FAILURE(String path) {
        String path_project1_session1 = path.replace("{projectId}", TicketingTestData.PROJECT_ID_1)
            .replace("{taskId}", TASK_ID_1)
            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path_project1_session1
                .replace("{messageId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(path_project1_session1
                .replace("{messageId}", CHAT_MESSAGE_ID_1))
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}" })
    void updateChatMessage_SUCCESS(String path) {
        String updatedMessageJson = "{\"content\":\"Updated Hello World\"}";

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("messageId", equalTo(CHAT_MESSAGE_ID_1))
            .body("sessionId", equalTo(EXAMPLE_CHAT_SESSION_ID_1))
            .body("senderId", equalTo(TicketingTestData.USER_ID_3))
            .body("contentType", equalTo("TEXT"))
            .body("content", equalTo("Updated Hello World"))
            .body("url", nullValue())
            .body("createdAt", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}" })
    void updateChatMessage_FAILURE(String path) {
        String path_project1_session1 = path.replace("{projectId}", TicketingTestData.PROJECT_ID_1)
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
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(put_request)
            .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .contentType(ContentType.JSON);

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .put(put_request)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .put(path_project1_session1
                .replace("{messageId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}" })
    void deleteChatMessage_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1, CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/{messageId}" })
    void deleteChatMessage_FAILURE(String path) {
        String path_project1_session1 = path.replace("{projectId}", TicketingTestData.PROJECT_ID_1)
            .replace("{taskId}", TASK_ID_1)
            .replace("{sessionId}", EXAMPLE_CHAT_SESSION_ID_1);
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path_project1_session1
                .replace("{messageId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .delete(path_project1_session1
                .replace("{messageId}", CHAT_MESSAGE_ID_1))
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path.replace("{projectId}", TicketingTestData.PROJECT_ID_1).replace("{taskId}", TASK_ID_1)
                .replace("{sessionId}", UUID.randomUUID().toString())
                .replace("{messageId}", CHAT_MESSAGE_ID_1))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload" })
    void uploadFile_SUCCESS(String path) throws Exception {
        Path tempDir = Files.createTempDirectory("test-upload");
        Path tempFile = tempDir.resolve("test-file.txt");
        Files.writeString(tempFile, "This is a test file content");
        String fileName = tempFile.getFileName().toString();
        String expectedBucketName = "remsfal-chat-files";
        try {
            Map<String, String> response =
                given()
                    .multiPart("file", tempFile.toFile(), MediaType.TEXT_PLAIN)
                    .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
                    .when()
                    .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                    .then()
                    .statusCode(Response.Status.CREATED.getStatusCode())
                    .contentType(ContentType.JSON)
                    .extract().as(new TypeRef<>() {
                    });

            String fileId = response.get("fileId");
            String fileUrl = response.get("fileUrl");
            String sessionId = response.get("sessionId");

            ChatMessageEntity persistedMessage = chatMessageController.getChatMessage(sessionId, fileId);

            assertNotNull(persistedMessage, "Persisted message should not be null");
            assertEquals(EXAMPLE_CHAT_SESSION_ID_1, persistedMessage.getSessionId().toString(),
                "Chat session ID should match");
            assertEquals(ChatMessageRepository.ContentType.FILE.name(), persistedMessage.getContentType(),
                "Content type should match");
            assertEquals(fileUrl, persistedMessage.getUrl(), "Persisted URL should match the returned file URL");
            verifyFileInBucket(expectedBucketName, fileName);

        } finally {
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }
    }

    private void verifyFileInBucket(String bucketName, String fileName) {
        boolean fileExists = false;
        try {
            Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(FileStorage.DEFAULT_BUCKET_NAME)
                    .build());
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
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload" })
    void uploadFile_MissingFilePart_FAILURE(String path) {
        given()
            .multiPart("someField", "someValue") // This ensures a valid multipart request with a boundary
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .when()
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .body("message", equalTo("No file part found in the form data"));

    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload" })
    void uploadFile_InvalidContentType_FAILURE(String path) throws Exception {
        Path tempFile = Files.createTempFile("test-file", ".exe"); // Unsupported file type
        try {
            given()
                .multiPart("file", tempFile.toFile(), "application/x-msdownload")
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
                .when()
                .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode())
                .body("message", containsString("Unsupported Media Type: application/x-msdownload"));

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload" })
    void uploadFile_EmptyInputStream_FAILURE(String path) {
        given()
            .multiPart("file", "", MediaType.TEXT_PLAIN) // Empty file content
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
            .when()
            .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .body("message", equalTo("Failed to read file stream: unknown"));
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_TASK_PATH_WITH_SESSION_ID + "/messages/upload" })
    void uploadFile_ChatSessionClosed_FAILURE(String path) throws Exception {
        logger.info("expected session id: " + EXAMPLE_CHAT_SESSION_ID_1);
        logger.info("actual session id: " + EXAMPLE_CHAT_SESSION_ID_1_UUID);
        Path tempFile = Files.createTempFile("test-file", ".txt");
        try {
            given()
                .multiPart("file", tempFile.toFile(), MediaType.TEXT_PLAIN)
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, Duration.ofMinutes(10)))
                .when()
                .post(path, TicketingTestData.PROJECT_ID_1, TASK_ID_1, EXAMPLE_CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private MultipartFormDataInput createMultipartFormDataInput(String fileName, byte[] content)
        throws Exception {
        MultipartFormDataInput input = mock(MultipartFormDataInput.class);
        InputPart inputPart = mock(InputPart.class);

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("Content-Disposition", "form-data; name=\"file\"; filename=\"" + fileName + "\"");

        when(inputPart.getHeaders()).thenReturn(headers);
        when(inputPart.getMediaType()).thenReturn(MediaType.valueOf(MediaType.TEXT_PLAIN));
        when(inputPart.getBody(InputStream.class, null)).thenReturn(new ByteArrayInputStream(content));
        when(input.getFormDataMap()).thenReturn(Map.of("file", List.of(inputPart)));

        return input;
    }
}
