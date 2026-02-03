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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.control.ChatMessageController;
import de.remsfal.ticketing.control.FileStorageController;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository;
import de.remsfal.ticketing.entity.storage.FileStorage;
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
class ChatMessageResourceTest extends AbstractTicketingTest {

    @Inject
    FileStorageController fileStorageService;

    @Inject
    ChatMessageController chatMessageController;

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    CqlSession cqlSession;

    static final String BASE_PATH = "/ticketing/v1/issues";
    static final String CHAT_SESSIONS_PATH = BASE_PATH + "/{issueId}/chats";
    static final String CHAT_SESSION_ID_PATH = CHAT_SESSIONS_PATH + "/{sessionId}";
    static final String CHAT_MESSAGES_PATH = CHAT_SESSION_ID_PATH + "/messages";
    static final String CHAT_MESSAGE_ID_PATH = CHAT_MESSAGES_PATH + "/{messageId}";
    static final String CHAT_UPLOAD_PATH = CHAT_MESSAGES_PATH + "/upload";

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
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_1,
            "Test Issue 1", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            TicketingTestData.USER_ID, null, TicketingTestData.USER_ID,
            "Test issue for chat session 1");
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_2,
            "Test Issue 2", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            TicketingTestData.USER_ID, null, TicketingTestData.USER_ID,
            "Test issue for chat session 2");
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

    @Test
    void sendMessage_FAILURE_INVALID_PAYLOAD() {
        String largePayload = "{\"content\":\"" + "a".repeat(9000) + "\"}";
        given()
            .body(largePayload)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .post(CHAT_MESSAGES_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void sendMessage_INVALID_INPUT() {
        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD_BLANK_CONTENT)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .post(CHAT_MESSAGES_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD_NULL_CONTENT)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .post(CHAT_MESSAGES_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void sendMessage_SUCCESS() {
        given()
            .body(CHAT_MESSAGE_JSON_PAYLOAD)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .post(CHAT_MESSAGES_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .body("messageId", notNullValue());
    }

    @Test
    void getChatMessages_SUCCESS() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .get(CHAT_MESSAGES_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
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

    @Test
    void getChatMessage_SUCCESS() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .get(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
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

    @Test
    void getChatMessage_FILETYPE_SUCCESS() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .get(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_3)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString("filename=\"" + TicketingTestData.FILE_PNG_PATH + "\""))
            .body(not(emptyString()));
    }

    @Test
    void getChatMessages_FAILURE() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .get(CHAT_MESSAGES_PATH, TicketingTestData.ISSUE_ID_1, UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_MESSAGES_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void getChatMessage_FAILURE() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .get(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, UUID.randomUUID())
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateChatMessage_SUCCESS() {
        String updatedMessageJson = "{\"content\":\"Updated Hello World\"}";

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .put(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
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

    @Test
    void updateChatMessage_FAILURE() {
        String updatedMessageJsonBlank = "{\"content\":\"\"}";
        String updatedMessageJson = "{\"content\":\"Updated Hello World\"}";

        given()
            .body(updatedMessageJsonBlank)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .put(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .contentType(ContentType.JSON);

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .put(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .body(updatedMessageJson)
            .contentType(ContentType.JSON)
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .put(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, UUID.randomUUID())
            .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    void deleteChatMessage_SUCCESS() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .delete(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void deleteChatMessage_FAILURE() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .delete(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, UUID.randomUUID())
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .delete(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .delete(CHAT_MESSAGE_ID_PATH, TicketingTestData.ISSUE_ID_1, UUID.randomUUID(), TicketingTestData.CHAT_MESSAGE_ID_1)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void uploadFile_SUCCESS() throws Exception {
        Path tempDir = Files.createTempDirectory("test-upload");
        Path tempFile = tempDir.resolve("test-file.txt");
        Files.writeString(tempFile, "This is a test file content");
        String fileName = tempFile.getFileName().toString();
        String expectedBucketName = FileStorage.DEFAULT_BUCKET_NAME;
        try {
            Map<String, String> response =
                given()
                    .multiPart("file", tempFile.toFile(), MediaType.TEXT_PLAIN)
                    .cookie(buildManagerCookie(rolesManagerP1()))
                    .when()
                    .post(CHAT_UPLOAD_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
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
            // Use the fileUrl as the object name for verification (remove leading slash if present)
            String objectName = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            verifyFileInBucket(expectedBucketName, objectName);

        } finally {
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }
    }

    private void verifyFileInBucket(String bucketName, String fileName) {
        boolean fileExists = false;
        StringBuilder foundFiles = new StringBuilder("Files in bucket: ");
        try {
            Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(true)
                    .build());
            for (Result<Item> item : items) {
                String objectName = item.get().objectName();
                foundFiles.append(objectName).append(", ");
                if (objectName.contains(fileName)) {
                    fileExists = true;
                    break;
                }
            }
        } catch (Exception e) {
            fail("Exception occurred while verifying the file in the bucket: " + e.getMessage(), e);
        }
        assertTrue(fileExists, "Uploaded file should exist in the bucket. " + foundFiles.toString() + " Searching for: " + fileName);
    }

    @Test
    void uploadFile_MissingFilePart_FAILURE() {
        given()
            .multiPart("someField", "someValue") // This ensures a valid multipart request with a boundary
            .cookie(buildManagerCookie(rolesManagerP1()))
            .when()
            .post(CHAT_UPLOAD_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .body("message", equalTo("No file part found in the form data"));
    }

    @Test
    void uploadFile_InvalidContentType_FAILURE() throws Exception {
        Path tempFile = Files.createTempFile("test-file", ".exe"); // Unsupported file type
        Files.write(tempFile, "dangerous code".getBytes(StandardCharsets.UTF_8));
        try {
            given()
                .multiPart("file", tempFile.toFile(), "application/x-msdownload")
                .cookie(buildManagerCookie(rolesManagerP1()))
                .when()
                .post(CHAT_UPLOAD_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode())
                .body("message", containsString("Unsupported Media Type: application/x-msdownload"));

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void uploadFile_EmptyInputStream_FAILURE() {
        given()
            .multiPart("file", "", MediaType.TEXT_PLAIN) // Empty file content
            .cookie(buildManagerCookie(rolesManagerP1()))
            .when()
            .post(CHAT_UPLOAD_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .body("message", equalTo("Failed to read file stream: unknown"));
    }

    @Test
    void uploadFile_ChatSessionClosed_FAILURE() throws Exception {
        logger.info("expected session id: " + TicketingTestData.CHAT_SESSION_ID_1);
        logger.info("actual session id: " + TicketingTestData.CHAT_SESSION_ID_1);
        Path tempFile = Files.createTempFile("test-file", ".txt");
        try {
            given()
                .multiPart("file", tempFile.toFile(), MediaType.TEXT_PLAIN)
                .cookie(buildManagerCookie(rolesManagerP1()))
                .when()
                .post(CHAT_UPLOAD_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

}