package de.remsfal.ticketing.boundary;

import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.awaitility.Awaitility;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.test.TestData;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.control.ChatMessageController;
import de.remsfal.ticketing.control.FileStorageController;
import de.remsfal.ticketing.control.IssueController;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.testcontainers.OcrServiceResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

/**
 * @author Christopher Keller [christopher.keller@student.htw-berlin.de]
 */
@QuarkusTest
@QuarkusTestResource(OcrServiceResource.class)
@QuarkusTestResource(CassandraTestResource.class)
@TestSecurity(user = "test-user")
public class OcrTest extends AbstractResourceTest {

    @InjectSpy
    ChatMessageController chatMessageController;

    @Inject
    ChatSessionResource chatSessionResource;

    @Inject
    ChatMessageResource chatMessageResource;

    @InjectMock
    IssueController issueController;

    @Inject
    FileStorageController fileStorageService;

    @InjectMock
    RemsfalPrincipal principal;

    @Test
    public void testOcrService_SUCCESS() throws Exception {
        InputStream imageStream = getClass().getClassLoader()
            .getResourceAsStream(TicketingTestData.FILE_PNG_PATH);
        byte[] fileContent = imageStream.readAllBytes();

        MultipartFormDataInput input = createMultipartFormDataInput(
            TicketingTestData.FILE_PNG_PATH, TicketingTestData.FILE_PNG_TYPE, fileContent);

        UUID taskId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        IssueEntity task = mock(IssueEntity.class);
        when(task.getProjectId()).thenReturn(TestData.PROJECT_ID);
        when(issueController.getIssue(eq(taskId))).thenReturn(task);
        when(principal.getId()).thenReturn(UUID.randomUUID());
        when(principal.getProjectRoles()).thenReturn(Map.of(TestData.PROJECT_ID, MemberRole.MANAGER));
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(jwt.getClaim("project_roles")).thenReturn(Map.of(TestData.PROJECT_ID, "MANAGER"));
        when(principal.getJwt()).thenReturn(jwt);

        Response response = chatMessageResource.uploadFile(taskId, sessionId, input);

        assertEquals(201, response.getStatus());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getEntity().toString());
        String messageId = root.get("fileId").asText();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> verify(chatMessageController, atLeastOnce())
                .updateTextChatMessage(eq(sessionId), eq(UUID.fromString(messageId)), anyString()));
    }

    @Test
    void testNullInput_Exception() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> chatMessageResource.extractFileNameFromUrl(null));
        assertEquals("File URL cannot be null or empty", ex.getMessage());
    }

    @Test
    void testBlankInput_Exception() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> chatMessageResource.extractFileNameFromUrl(" "));
        assertEquals("File URL cannot be null or empty", ex.getMessage());
    }

    @Test
    void testEndsWithSlash_Exception() {
        String input = "https://example.com/files/";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> chatMessageResource.extractFileNameFromUrl(input));
        assertEquals("Invalid file URL format: " + input, ex.getMessage());
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
