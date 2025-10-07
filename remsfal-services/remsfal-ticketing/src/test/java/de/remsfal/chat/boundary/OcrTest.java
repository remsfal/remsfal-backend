package de.remsfal.chat.boundary;

import com.datastax.oss.quarkus.test.CassandraTestResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.remsfal.chat.resource.OcrServiceResource;
import de.remsfal.chat.TicketingTestData;
import de.remsfal.chat.control.ChatMessageController;
import de.remsfal.chat.control.FileStorageController;
import de.remsfal.common.authentication.RemsfalPrincipal;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Map;
import java.util.UUID;

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

        String projectId = UUID.randomUUID().toString();
        String taskId = UUID.randomUUID().toString();
        String sessionId = UUID.randomUUID().toString();

        when(principal.getId()).thenReturn(UUID.randomUUID());
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(jwt.getClaim("project_roles")).thenReturn(Map.of(projectId, "MANAGER"));
        when(principal.getJwt()).thenReturn(jwt);

        Response response = chatSessionResource.uploadFile(projectId, taskId, sessionId, input);

        assertEquals(201, response.getStatus());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getEntity().toString());
        String messageId = root.get("fileId").asText();

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() ->
                        verify(chatMessageController, atLeastOnce())
                                .updateTextChatMessage(eq(sessionId), eq(messageId), eq("Das hier ist ein Text"))
                );
    }

    @Test
    void testNullInput_Exception() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                chatSessionResource.extractFileNameFromUrl(null)
        );
        assertEquals("File URL cannot be null or empty", ex.getMessage());
    }

    @Test
    void testBlankInput_Exception() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                chatSessionResource.extractFileNameFromUrl(" ")
        );
        assertEquals("File URL cannot be null or empty", ex.getMessage());
    }

    @Test
    void testEndsWithSlash_Exception() {
        String input = "https://example.com/files/";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                chatSessionResource.extractFileNameFromUrl(input)
        );
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
