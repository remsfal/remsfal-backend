package de.remsfal.chat.boundary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.remsfal.chat.resource.OcrServiceResource;
import de.remsfal.chat.control.AuthorizationController;
import de.remsfal.chat.control.ChatMessageController;
import de.remsfal.chat.control.FileStorageService;
import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.ProjectMemberModel;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.awaitility.Awaitility;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
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
public class OcrTest {

    private static final String BUCKET_NAME = "remsfal-chat-files";

    @InjectMock
    AuthorizationController authorizationController;

    @InjectSpy
    ChatMessageController chatMessageController;

    @Inject
    ChatSessionResource chatSessionResource;

    @Inject
    FileStorageService fileStorageService;

    @Inject
    MinioClient minioClient;

    @InjectMock
    RemsfalPrincipal principal;

    @BeforeEach
    public void setup(){
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            }
        } catch (Exception e) {
            fail("Failed to initialize MinIO bucket: " + e.getMessage());
        }
    }

    @AfterEach
    public void cleanup() throws Exception {
        Iterable<Result<Item>> results = fileStorageService.listObjects(BUCKET_NAME);
        for (Result<Item> result : results) {
            Item item = result.get();
            fileStorageService.deleteObject(BUCKET_NAME, item.objectName());
        }
    }

    @Test
    public void testOcrService_SUCCESS() throws Exception {
        String fileName = "test-image.png";
        String contentType = "image/png";

        InputStream imageStream = getClass().getClassLoader().getResourceAsStream(fileName);
        byte[] fileContent = imageStream.readAllBytes();

        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);

        String projectId = UUID.randomUUID().toString();
        String taskId = UUID.randomUUID().toString();
        String sessionId = UUID.randomUUID().toString();

        when(principal.getId()).thenReturn(UUID.randomUUID().toString());

        var mockedRole = mock(ProjectMemberModel.MemberRole.class);
        when(mockedRole.isPrivileged()).thenReturn(true);
        when(authorizationController.getProjectMemberRole(any(), anyString()))
                .thenReturn(mockedRole);

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
    void testNoSlashInUrl_Exception() {
        String input = "filename.png";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                chatSessionResource.extractFileNameFromUrl(input)
        );
        assertEquals("Invalid file URL format: " + input, ex.getMessage());
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




