package de.remsfal.ticketing.boundary;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.control.ChatMessageController;
import de.remsfal.ticketing.control.FileStorageController;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository;
import de.remsfal.ticketing.entity.dao.FileStorage;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository.ParticipantRole;
import de.remsfal.ticketing.entity.dto.ChatMessageEntity;
import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ChatMessageResourceTest extends AbstractResourceTest {

    @Inject
    FileStorageController fileStorageService;

    @Inject
    ChatMessageController chatMessageController;

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    CqlSession cqlSession;

    static final String BASE_PATH = "/ticketing/v1/issues";
    static final String CHAT_SESSION_PATH = BASE_PATH + "/{issueId}/chats";
    static final String CHAT_SESSION_ID_PATH = CHAT_SESSION_PATH + "/{sessionId}";
    static final String CHAT_MESSAGES_PATH = CHAT_SESSION_ID_PATH + "/messages";

    static final String CHAT_MESSAGE_JSON_PAYLOAD = "{"
        + "\"session_id\": \"" + TicketingTestData.CHAT_SESSION_ID_1 + "\","
        + "\"sender_id\": \"" + TicketingTestData.USER_ID + "\","
        + "\"contentType\": \"" + ContentType.TEXT.name() + "\","
        + "\"content\": \"Test Message\""
        + "}";
    static final String CHAT_MESSAGE_JSON_PAYLOAD_BLANK_CONTENT = "{"
        + "\"session_id\": \"" + TicketingTestData.CHAT_SESSION_ID_1 + "\","
        + "\"sender_id\": \"" + TicketingTestData.USER_ID + "\","
        + "\"contentType\": \"" + ContentType.TEXT.name() + "\","
        + "\"content\": \"\""
        + "}";
    static final String CHAT_MESSAGE_JSON_PAYLOAD_NULL_CONTENT = "{"
        + "\"session_id\": \"" + TicketingTestData.CHAT_SESSION_ID_1 + "\","
        + "\"sender_id\": \"" + TicketingTestData.USER_ID + "\","
        + "\"contentType\": \"" + ContentType.TEXT.name() + "\","
        + "\"content\": null"
        + "}";

    @BeforeEach
    protected void setup() throws Exception {
        logger.info("Setting up test data");
        super.setupTestFiles();
        logger.info("Setting up issues for chat sessions");
        String insertIssueCql = "INSERT INTO remsfal.issues " +
            "(project_id, issue_id, type, title, status, reporter_id, owner_id, description, created_by, created_at, modified_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertIssueCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_1,
            "TASK", "Test Issue 1", "OPEN",
            TicketingTestData.USER_ID, TicketingTestData.USER_ID,
            "Test issue for chat session 1",
            TicketingTestData.USER_ID, Instant.now(), Instant.now());
        cqlSession.execute(insertIssueCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_2,
            "TASK", "Test Issue 2", "OPEN",
            TicketingTestData.USER_ID, TicketingTestData.USER_ID,
            "Test issue for chat session 2",
            TicketingTestData.USER_ID, Instant.now(), Instant.now());
        logger.info("Setting up chat sessions and messages");
        String insertChatSessionCql = "INSERT INTO remsfal.chat_sessions " +
            "(project_id, issue_id, session_id, created_at, participants) " +
            "VALUES (?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatSessionCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1,
            Instant.now(),
            Map.of(
                TicketingTestData.USER_ID_4, ParticipantRole.INITIATOR.name(),
                TicketingTestData.USER_ID_3, ParticipantRole.HANDLER.name()));
        cqlSession.execute(insertChatSessionCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_2, TicketingTestData.CHAT_SESSION_ID_2,
            Instant.now(),
            Map.of(
                TicketingTestData.USER_ID_4, ParticipantRole.INITIATOR.name(),
                TicketingTestData.USER_ID_3, ParticipantRole.HANDLER.name()));
        
        String insertChatMessageCql = "INSERT INTO remsfal.chat_messages " +
            "(session_id, message_id, sender_id, content_type, content, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatMessageCql,
            TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1, TicketingTestData.USER_ID_3,
            ChatMessageRepository.ContentType.TEXT.name(), "Hello World", Instant.now());

        cqlSession.execute(insertChatMessageCql,
            TicketingTestData.CHAT_SESSION_ID_2, TicketingTestData.CHAT_MESSAGE_ID_2, TicketingTestData.USER_ID_4,
            ChatMessageRepository.ContentType.TEXT.name(), "Hello World", Instant.now());
        
        String insertChatMessageCql3 = "INSERT INTO remsfal.chat_messages " +
            "(session_id, message_id, sender_id, content_type, url, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatMessageCql3,
            TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_3, TicketingTestData.USER_ID_3,
            ChatMessageRepository.ContentType.FILE.name(), TicketingTestData.FILE_PNG_PATH, Instant.now());
    }

    private Map<String, String> rolesManagerP1() {
        return Map.of(TicketingTestData.PROJECT_ID_1.toString(), "MANAGER");
    }

    private Map<String, String> rolesNone() {
        return Map.of();
    }

    private String nameOf(String first, String last) {
        return first + " " + last;
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_MESSAGES_PATH })
    void sendMessage_FAILURE_INVALID_PAYLOAD(String path) {
        String largePayload = "{\"content\":\"" + "a".repeat(9000) + "\"}";
        given()
            .body(largePayload)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .post(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_MESSAGES_PATH })
    void sendMessage_INVALID_INPUT(String path) {
        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD_BLANK_CONTENT)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .post(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD_NULL_CONTENT)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .post(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_MESSAGES_PATH })
    void sendMessage_SUCCESS(String path) {
        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .post(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .body("messageId", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_MESSAGES_PATH })
    void getChatMessages_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("session_id", equalTo(TicketingTestData.CHAT_SESSION_ID_1.toString()))
            .body("issue_id", equalTo(TicketingTestData.ISSUE_ID_1.toString()))
            .body("project_id", equalTo(TicketingTestData.PROJECT_ID_1.toString()))
            .body("messages[1].DATETIME", notNullValue())
            .body("messages[1].message_id", notNullValue())
            .body("messages[1].SENDER_ID", equalTo(TicketingTestData.USER_ID_3.toString()))
            .body("messages[1].MEMBER_ROLE", equalTo("HANDLER"))
            .body("messages[1].MESSAGE_TYPE", equalTo("TEXT"))
            .body("messages[1].MESSAGE_CONTENT", equalTo("Hello World"));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/{messageId}" })
    void getChatMessage_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("messageId", equalTo(TicketingTestData.CHAT_MESSAGE_ID_1.toString()))
            .body("sessionId", equalTo(TicketingTestData.CHAT_SESSION_ID_1.toString()))
            .body("senderId", equalTo(TicketingTestData.USER_ID_3.toString()))
            .body("contentType", equalTo("TEXT"))
            .body("content", equalTo("Hello World"))
            .body("url", nullValue())
            .body("createdAt", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/{messageId}" })
    void getChatMessage_FILETYPE_SUCCESS(String path) {
        String resolvedPath = path
            .replace("{issueId}", TicketingTestData.ISSUE_ID_1.toString())
            .replace("{sessionId}", TicketingTestData.CHAT_SESSION_ID_1.toString())
            .replace("{messageId}", TicketingTestData.CHAT_MESSAGE_ID_3.toString());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(resolvedPath)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString("filename=\"" + TicketingTestData.FILE_PNG_PATH + "\""))
            .body(not(emptyString()));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_MESSAGES_PATH })
    void getChatMessages_FAILURE(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(path, TicketingTestData.ISSUE_ID_1.toString(), UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .get(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/{messageId}" })
    void getChatMessage_FAILURE(String path) {
        String path_project1_session1 = path
            .replace("{issueId}", TicketingTestData.ISSUE_ID_1.toString())
            .replace("{sessionId}", TicketingTestData.CHAT_SESSION_ID_1.toString());
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(path_project1_session1
                .replace("{messageId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .get(path_project1_session1
                .replace("{messageId}", TicketingTestData.CHAT_MESSAGE_ID_1.toString()))
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/{messageId}" })
    void updateChatMessage_SUCCESS(String path) {
        String updatedMessageJson = "{\"content\":\"Updated Hello World\"}";

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .put(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("messageId", equalTo(TicketingTestData.CHAT_MESSAGE_ID_1.toString()))
            .body("sessionId", equalTo(TicketingTestData.CHAT_SESSION_ID_1.toString()))
            .body("senderId", equalTo(TicketingTestData.USER_ID_3.toString()))
            .body("contentType", equalTo("TEXT"))
            .body("content", equalTo("Updated Hello World"))
            .body("url", nullValue())
            .body("createdAt", notNullValue());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/{messageId}" })
    void updateChatMessage_FAILURE(String path) {
        String path_project1_session1 = path
            .replace("{issueId}", TicketingTestData.ISSUE_ID_1.toString())
            .replace("{sessionId}", TicketingTestData.CHAT_SESSION_ID_1.toString());
        String updatedMessageJsonBlank = "{\"content\":\"\"}";
        String updatedMessageJson = "{\"content\":\"Updated Hello World\"}";
        String put_request = path_project1_session1
            .replace("{messageId}", TicketingTestData.CHAT_MESSAGE_ID_1.toString());

        given()
            .body(updatedMessageJsonBlank)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .put(put_request)
            .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .contentType(ContentType.JSON);

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .put(put_request)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .put(path_project1_session1
                .replace("{messageId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/{messageId}" })
    void deleteChatMessage_SUCCESS(String path) {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .delete(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/{messageId}" })
    void deleteChatMessage_FAILURE(String path) {
        String path_project1_session1 = path
            .replace("{issueId}", TicketingTestData.ISSUE_ID_1.toString())
            .replace("{sessionId}", TicketingTestData.CHAT_SESSION_ID_1.toString());
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .delete(path_project1_session1
                .replace("{messageId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .delete(path_project1_session1
                .replace("{messageId}", TicketingTestData.CHAT_MESSAGE_ID_1.toString()))
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .delete(path.replace("{issueId}", TicketingTestData.ISSUE_ID_1.toString())
                .replace("{sessionId}", UUID.randomUUID().toString())
                .replace("{messageId}", TicketingTestData.CHAT_MESSAGE_ID_1.toString()))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/upload" })
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
                    .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                            nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                            rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
                    .when()
                    .post(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
                    .then()
                    .statusCode(Response.Status.CREATED.getStatusCode())
                    .contentType(ContentType.JSON)
                    .extract().as(new TypeRef<>() {
                    });

            String fileId = response.get("fileId");
            String fileUrl = response.get("fileUrl");
            String sessionId = response.get("sessionId");

            ChatMessageEntity persistedMessage = chatMessageController.getChatMessage(UUID.fromString(sessionId), UUID.fromString(fileId));

            assertNotNull(persistedMessage, "Persisted message should not be null");
            assertEquals(TicketingTestData.CHAT_SESSION_ID_1, persistedMessage.getSessionId(),
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
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/upload" })
    void uploadFile_MissingFilePart_FAILURE(String path) {
        given()
            .multiPart("someField", "someValue") // This ensures a valid multipart request with a boundary
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .when()
            .post(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .body("message", equalTo("No file part found in the form data"));
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/upload" })
    void uploadFile_InvalidContentType_FAILURE(String path) throws Exception {
        Path tempFile = Files.createTempFile("test-file", ".exe"); // Unsupported file type
        try {
            given()
                .multiPart("file", tempFile.toFile(), "application/x-msdownload")
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                        rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
                .when()
                .post(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode())
                .body("message", containsString("Unsupported Media Type: application/x-msdownload"));

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/upload" })
    void uploadFile_EmptyInputStream_FAILURE(String path) {
        given()
            .multiPart("file", "", MediaType.TEXT_PLAIN) // Empty file content
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .when()
            .post(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .body("message", equalTo("Failed to read file stream: unknown"));
    }

    @ParameterizedTest
    @ValueSource(strings = { CHAT_SESSION_ID_PATH + "/messages/upload" })
    void uploadFile_ChatSessionClosed_FAILURE(String path) throws Exception {
        logger.info("expected session id: " + TicketingTestData.CHAT_SESSION_ID_1);
        logger.info("actual session id: " + TicketingTestData.CHAT_SESSION_ID_1);
        Path tempFile = Files.createTempFile("test-file", ".txt");
        try {
            given()
                .multiPart("file", tempFile.toFile(), MediaType.TEXT_PLAIN)
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                        rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
                .when()
                .post(path, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

}