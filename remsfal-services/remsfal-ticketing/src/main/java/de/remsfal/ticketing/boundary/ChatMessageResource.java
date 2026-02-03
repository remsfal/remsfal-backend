package de.remsfal.ticketing.boundary;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.common.model.FileUploadData;
import de.remsfal.common.validation.MediaTypeValidator;
import de.remsfal.core.api.ticketing.ChatMessageEndpoint;
import de.remsfal.core.json.ticketing.ChatMessageJson;
import de.remsfal.ticketing.control.ChatMessageController;
import de.remsfal.ticketing.control.ChatSessionController;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository.ContentType;
import de.remsfal.ticketing.entity.dto.ChatMessageEntity;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class ChatMessageResource extends AbstractTicketingResource implements ChatMessageEndpoint {

    @Inject
    ChatSessionController chatSessionController;

    @Inject
    ChatMessageController chatMessageController;

    @Inject
    Logger logger;

    private static final String NOT_FOUND_SESSION_MESSAGE = "Chat session not found";

    @Override
    public Response sendMessage(final UUID issueId,
        final UUID sessionId, final ChatMessageJson message) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            UUID userId = principal.getId();
            if (userId == null) {
                throw new NotAuthorizedException("No user authentication provided via session cookie");
            }
            Map<UUID, String> participants =
                chatSessionController.getParticipants(projectId, issueId, sessionId);
            boolean isParticipant = participants.containsKey(userId);
            boolean hasWritePermission = false;
            try {
                hasWritePermission = true;
            } catch (NotAuthorizedException | ForbiddenException ignored) {
                // User does not have write permissions
            }
            if (!isParticipant && !hasWritePermission) {
                throw new ForbiddenException("Inadequate user rights");
            }
            if (message.getContent() == null || message.getContent().isBlank()) {
                throw new BadRequestException("Message content cannot be null or empty");
            }
            int maxPayloadSize = 8000;
            if (message.getContent().length() > maxPayloadSize) {
                throw new BadRequestException("Payload size exceeds limit");
            }
            ChatMessageEntity entity = chatMessageController.sendChatMessage(
                sessionId, userId, message.getContentType(), message.getContent());
            URI location = uri.getAbsolutePathBuilder()
                .path(entity.getMessageId().toString()).build();
            return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(ChatMessageJson.valueOf(entity))
                .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to send message", e);
            throw e;
        }
    }

    @Override
    public Response getChatMessages(final UUID issueId,
        final UUID sessionId) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(chatSessionController.getChatLogs(projectId, sessionId, issueId))
                .build();

        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get chat messages", e);
            throw e;
        }

    }

    @Override
    public Response getChatMessage(final UUID issueId,
        final UUID sessionId, final UUID messageId) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            ChatMessageEntity chatMessageEntity =
                chatMessageController.getChatMessage(sessionId, messageId);
            if (chatMessageEntity.getContentType().equals(ContentType.TEXT.name()))
                return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatMessageJson.valueOf(chatMessageEntity))
                    .build();
            if (chatMessageEntity.getContentType().equals(ContentType.FILE.name())) {
                // Extract the file URL and derive the file name
                String fileUrl = chatMessageEntity.getUrl();
                String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

                // Download the file from the storage service via controller
                InputStream fileStream = chatMessageController.downloadFile(sessionId, messageId);
                // Stream the file content to the client as a binary response
                return Response.ok((StreamingOutput) output -> {
                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    // Read chunks from the file and write them to the HTTP response
                    while (true) {
                        bytesRead = fileStream.read(buffer);
                        if (bytesRead == -1) {
                            break; // End of file reached
                        }
                        output.write(buffer, 0, bytesRead);
                    }
                })
                    .type(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
            }
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get chat message", e);
            throw e;
        }
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("{\"message\": \"The file type is not recognized\"}")
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    @Override
    public Response updateChatMessage(final UUID issueId,
        final UUID sessionId,
        final UUID messageId,
        final ChatMessageJson message) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            int maxPayloadSize = 8000;
            if (Objects.requireNonNull(message.getContent()).length() > maxPayloadSize) {
                throw new BadRequestException("Payload size exceeds limit");
            }
            chatMessageController.updateTextChatMessage(sessionId, messageId, message.getContent());
            ChatMessageEntity updatedMessage =
                chatMessageController.getChatMessage(sessionId, messageId);
            if (updatedMessage.getContent().equals(message.getContent())) {
                return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatMessageJson.valueOf(updatedMessage))
                    .build();
            } else {
                throw new InternalServerErrorException("Failed to update message");
            }
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to update message", e);
            throw e;
        }
    }

    @Override
    public Response deleteChatMessage(final UUID issueId,
        final UUID sessionId, final UUID messageId) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            if (chatSessionController.getChatSession(projectId, issueId, sessionId).isPresent()) {
                chatMessageController.deleteChatMessage(sessionId, messageId);
                return Response.noContent().build();
            } else {
                throw new NotFoundException(NOT_FOUND_SESSION_MESSAGE);
            }
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete message", e);
            throw e;
        }
    }

    @Override
    public Response uploadFile(final UUID issueId,
        final UUID sessionId, final MultipartFormDataInput input) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
            List<InputPart> fileParts = formDataMap.get("file");
            if (fileParts == null || fileParts.isEmpty()) {
                logger.error("No 'file' part found in the form data");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"No file part found in the form data\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }

            for (InputPart inputPart : fileParts) {
                String fileName = getFileName(inputPart.getHeaders());
                if (!isFileNameValid(fileName)) {
                    logger.error("Invalid file name: " + fileName);
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid file name: " + fileName + "\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
                }

                // Validate content type before processing
                MediaType contentType = inputPart.getMediaType();
                if (!MediaTypeValidator.isValid(contentType)) {
                    logger.error("Invalid file type: " + contentType);
                    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                        .entity("{\"message\": \"Unsupported Media Type: " + contentType + "\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
                }

                // Extract file data from InputPart and create transport-neutral wrapper
                try {
                    InputStream fileStream = inputPart.getBody(InputStream.class, null);
                    if (fileStream == null || fileStream.available() == 0) {
                        logger.error("File stream is null or empty");
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\": \"Failed to read file stream: unknown\"}")
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                    }

                    FileUploadData fileData = new FileUploadData(fileStream, fileName, contentType);

                    // Upload file via controller
                    ChatMessageEntity fileMetadataEntity =
                        chatMessageController.uploadFile(principal, sessionId, fileData);

                    String fileUrl = fileMetadataEntity.getUrl();
                    String mergedJson = String.format(
                        "{\"fileId\": \"%s\", \"fileUrl\": \"%s\", \"sessionId\":" +
                        " \"%s\", \"createdAt\": \"%s\", \"sender\": \"%s\"}",
                        fileMetadataEntity.getMessageId(),
                        fileUrl,
                        fileMetadataEntity.getSessionId(),
                        fileMetadataEntity.getCreatedAt(),
                        fileMetadataEntity.getSenderId());

                    return Response.status(Response.Status.CREATED)
                        .entity(mergedJson)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
                } catch (IOException e) {
                    logger.error("Error reading input part", e);
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Failed to read file data\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
                }
            }

            throw new BadRequestException("No valid file uploaded");

        } catch (Exception e) {
            logger.error("Error during file upload", e);
            throw new RuntimeException(e);
        }
    }

    // ---------------------Helper Methods---------------------

    private boolean isFileNameValid(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        return fileName.matches("^[\\w\\-. ]+$");
    }

}