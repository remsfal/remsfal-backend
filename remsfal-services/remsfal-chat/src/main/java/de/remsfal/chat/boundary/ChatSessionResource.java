package de.remsfal.chat.boundary;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.remsfal.chat.control.ChatMessageController;
import de.remsfal.chat.control.ChatSessionController;
import de.remsfal.chat.control.FileStorageService;
import de.remsfal.chat.entity.dao.ChatMessageRepository.ContentType;
import de.remsfal.chat.entity.dao.ChatSessionRepository.ParticipantRole;
import de.remsfal.chat.entity.dao.ChatSessionRepository.Status;
import de.remsfal.chat.entity.dao.ChatSessionRepository.TaskType;
import de.remsfal.chat.entity.dto.ChatMessageEntity;
import de.remsfal.chat.entity.dto.ChatSessionEntity;
import de.remsfal.core.api.chat.ChatSessionEndpoint;
import de.remsfal.core.json.chat.ChatMessageJson;
import de.remsfal.core.json.chat.ChatSessionJson;
import de.remsfal.core.model.chat.ChatSessionModel;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@RequestScoped
public class ChatSessionResource extends ChatSubResource implements ChatSessionEndpoint {

    @Inject
    ChatSessionController chatSessionController;

    @Inject
    ChatMessageController chatMessageController;

    @Inject
    Logger logger;

    @Inject
    FileStorageService fileStorageService;

    private final String bucketName = "remsfal-chat-files";
    private static final String NOT_FOUND_SESSION_MESSAGE = "Chat session not found";
    private static final String TASK_ID_CONSTANT = "taskId";
    private static final String DEFECT_ID_CONSTANT = "defectId";

    @Override
    public Response createChatSession(final String projectId) {
        try {
            checkWritePermissions(projectId);
            String userId = principal.getId(); // Get user ID from session
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            TaskType taskType = (taskId != null && !taskId.isBlank()) ? TaskType.TASK : TaskType.DEFECT;
            String associatedId = (taskId != null && !taskId.isBlank()) ? taskId : defectId;

            ChatSessionModel session = chatSessionController
                .createChatSession(projectId, associatedId, taskType.name(), userId);
            URI location = uri.getAbsolutePathBuilder().path(session.getSessionId().toString()).build();
            return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(ChatSessionJson.valueOf(session))
                .build();
        } catch (Exception e) {
            logger.error("Failed to create chat session", e);
            throw e;
        }
    }

    @Override
    public Response getChatSession(final String projectId, final String sessionId) {
        try {
            checkWritePermissions(projectId);
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            String id = (taskId == null || taskId.isBlank()) ? defectId : taskId;
            Optional<ChatSessionEntity> session = chatSessionController
                .getChatSession(projectId, id, sessionId);
            if (session.isPresent())
                return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatSessionJson.valueOf(session.get()))
                    .build();
            else
                throw new NoSuchElementException(NOT_FOUND_SESSION_MESSAGE);
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get chat session", e);
            throw e;
        }
    }

    @Override
    public Response deleteChatSession(final String projectId, final String sessionId) {
        try {
            checkWritePermissions(projectId);
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            String id = (taskId == null || taskId.isBlank()) ? defectId : taskId;
            chatSessionController.deleteChatSession(projectId, id, sessionId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete chat session", e);
            throw e;
        }
    }

    @Override
    public Response updateChatSessionStatus(final String projectId,
        final String sessionId, String status) {
        try {
            checkWritePermissions(projectId);
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            if (status.startsWith("\"") && status.endsWith("\"")) {
                status = status.substring(1, status.length() - 1);
            }
            if (!Objects.equals(status, Status.OPEN.name())
                && !Objects.equals(status, Status.CLOSED.name())) {
                throw new BadRequestException("Status must be provided");
            }
            String id = (taskId == null || taskId.isBlank()) ? defectId : taskId;
            chatSessionController.updateChatSessionStatus(projectId, id, sessionId, Status.valueOf(status));
            Optional<ChatSessionEntity> updatedSession =
                chatSessionController.getChatSession(projectId, id, sessionId);
            if (updatedSession.isPresent())
                return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatSessionJson.valueOf(updatedSession.get()))
                    .build();
            else
                throw new NoSuchElementException(NOT_FOUND_SESSION_MESSAGE);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to update chat session status", e);
            throw e;
        }
    }

    @Override
    public Response joinChatSession(final String projectId, final String sessionId) {
        try {
            checkWritePermissions(projectId);
            String userId = principal.getId(); // Get user ID from session
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            String id = (taskId == null || taskId.isBlank()) ? defectId : taskId;
            chatSessionController.addParticipant(projectId, id,
                sessionId, userId, ParticipantRole.OBSERVER);
            Map<UUID, String> participants =
                chatSessionController.getParticipants(projectId, id, sessionId);
            String json = jsonifyParticipantsMap(participants);
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to join chat session", e);
            throw e;
        }
    }

    @Override
    public Response getParticipants(final String projectId, final String sessionId) {
        try {
            checkWritePermissions(projectId);
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            String id = (taskId == null || taskId.isBlank()) ? defectId : taskId;
            Map<UUID, String> participants =
                chatSessionController.getParticipants(projectId, id, sessionId);
            String json = jsonifyParticipantsMap(participants);
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get participants", e);
            throw e;
        }
    }

    @Override
    public Response getParticipant(final String projectId, final String sessionId, final String participantId) {
        try {
            checkWritePermissions(projectId);
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            UUID participantUUID = UUID.fromString(participantId);
            String id = (taskId == null || taskId.isBlank()) ? defectId : taskId;
            Map<UUID, String> participants =
                chatSessionController.getParticipants(projectId, id, sessionId);
            if (participants.containsKey(participantUUID)) {
                String json = jsonifyParticipantsMap(Map.of(participantUUID, participants.get(participantUUID)));
                return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(json)
                    .build();
            } else {
                throw new NoSuchElementException("Participant not found");
            }
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get participant", e);
            throw e;
        }
    }

    @Override
    public Response changeParticipantRole(final String projectId, final String sessionId, final String participantId,
        String role) {
        try {
            checkWritePermissions(projectId);
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            role = cleanRole(role);
            UUID participantUUID = UUID.fromString(participantId);
            Map<UUID, String> participants =
                getParticipantsMap(projectId, sessionId, taskId, defectId);
            validateParticipant(participants, participantUUID);
            validateRole(role);
            updateParticipantRole(projectId, sessionId, taskId, defectId, participantId, role);
            validateRoleUpdate(projectId, sessionId, taskId, defectId, participantId, role);
            String json = jsonifyParticipantsMap(Map.of(participantUUID, role));
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to change participant role", e);
            throw e;
        }
    }

    @Override
    public Response removeParticipant(final String projectId, final String sessionId, final String participantId) {
        try {
            checkWritePermissions(projectId);
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            UUID participantUUID = UUID.fromString(participantId);
            String id = (taskId == null || taskId.isBlank()) ? defectId : taskId;
            Map<UUID, String> participants = chatSessionController
                .getParticipants(projectId, id, sessionId);
            if (!participants.containsKey(participantUUID)) {
                throw new NotFoundException("Participant not found");
            }
            chatSessionController
                .removeParticipant(projectId, id, sessionId, participantId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to remove participant from chat session", e);
            throw e;
        }
    }

    @Override
    public Response sendMessage(final String projectId,
        final String sessionId,
        final ChatMessageJson message) {
        try {
            checkWritePermissions(projectId);
            String userId = principal.getId(); // Get user ID from session
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
        } catch (BadRequestException e) {
            throw new BadRequestException("Malformed JSON payload");
        } catch (Exception e) {
            logger.error("Failed to send message", e);
            throw e;
        }
    }

    @Override
    public Response getChatMessages(final String projectId,
        final String sessionId) {
        try {
            checkWritePermissions(projectId);
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            String id = (taskId == null || taskId.isBlank()) ? defectId : taskId;
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(chatSessionController.exportChatLogs(projectId, sessionId, id))
                .build();

        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get chat messages", e);
            throw e;
        }

    }

    @Override
    public Response getChatMessage(final String projectId,
        final String sessionId,
        final String messageId) {
        try {
            checkWritePermissions(projectId);
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

                // Download the file from the storage service
                InputStream fileStream = fileStorageService.downloadFile(bucketName, fileName);
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
    public Response updateChatMessage(final String projectId, final String sessionId,
        final String messageId,
        final ChatMessageJson message) {
        try {
            checkWritePermissions(projectId);
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
    public Response deleteChatMessage(
        final String projectId,
        final String sessionId, final String messageId) {
        try {
            checkWritePermissions(projectId);
            String taskId = uri.getPathParameters().getFirst(TASK_ID_CONSTANT);
            String defectId = uri.getPathParameters().getFirst(DEFECT_ID_CONSTANT);
            validateTaskOrDefect(taskId, defectId);
            String id = (taskId == null || taskId.isBlank()) ? defectId : taskId;
            if (chatSessionController.getChatSession(projectId, id, sessionId).isPresent()) {
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
    public Response uploadFile(
        final String projectId,
        final String sessionId,
        final MultipartFormDataInput input) {
        try {
            String userId = principal.getId();
            checkWritePermissions(projectId);
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

                String contentType = inputPart.getMediaType().toString();
                if (!isContentTypeValid(contentType)) {
                    logger.error("Invalid file type: " + contentType);
                    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                        .entity("{\"message\": \"Unsupported Media Type: "
                            + contentType + "\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
                }

                try (InputStream fileStream = inputPart.getBody(InputStream.class, null)) {
                    if (fileStream == null || fileStream.available() == 0) {
                        logger.error("File stream is null or empty");
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\": \"Failed to read file stream: unknown\"}")
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                    }

                    String fileUrl = fileStorageService.uploadFile(bucketName, input);

                    ChatMessageEntity fileMetadataEntity = chatMessageController
                        .sendChatMessage(sessionId, userId, ContentType.FILE.name(), fileUrl);
                    String mergedJson = String.format(
                        "{\"fileId\": \"%s\", \"fileUrl\": \"%s\", \"sessionId\":" +
                            " \"%s\", \"createdAt\": \"%s\", \"sender\": \"%s\"}",
                        fileMetadataEntity.getMessageId(),
                        fileUrl,
                        fileMetadataEntity.getChatSessionId(),
                        fileMetadataEntity.getCreatedAt(),
                        fileMetadataEntity.getSenderId());

                    return Response.status(Response.Status.CREATED)
                        .entity(mergedJson)
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

    private String jsonifyParticipantsMap(Map<UUID, String> participants) {
        List<Map<String, String>> participantList = new ArrayList<>();
        participants.forEach((id, role) -> {
            Map<String, String> participant = new HashMap<>();
            participant.put("userId", id.toString());
            participant.put("userRole", role);
            participantList.add(participant);
        });
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(participantList);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    private String getFileName(Map<String, List<String>> headers) {
        List<String> contentDisposition = headers.get("Content-Disposition");
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            for (String part : contentDisposition.get(0).split(";")) {
                if (part.trim().startsWith("filename")) {
                    return part.split("=")[1].trim().replaceAll("\"", "");
                }
            }
        }
        return "unknown";
    }

    private boolean isFileNameValid(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        return fileName.matches("^[\\w\\-. ]+$");
    }

    private boolean isContentTypeValid(String contentType) {
        if (contentType == null) {
            return false;
        }
        // Normalize content type (remove charset if present)
        String normalizedContentType = contentType.split(";")[0].trim();
        Set<String> allowedTypes = fileStorageService.getAllowedTypes();
        return allowedTypes.contains(normalizedContentType);
    }

    private void validateTaskOrDefect(String taskId, String defectId) {
        if ((taskId == null || taskId.isBlank()) && (defectId == null || defectId.isBlank())) {
            throw new BadRequestException("Task ID or defect ID must be provided");
        }
    }

    private String cleanRole(String role) {
        if (role.startsWith("\"") && role.endsWith("\"")) {
            return role.substring(1, role.length() - 1);
        }
        return role;
    }

    private Map<UUID, String> getParticipantsMap(String projectId, String sessionId, String taskId, String defectId) {
        return (taskId == null || taskId.isBlank()) ? chatSessionController.getParticipants(projectId, defectId, sessionId) : chatSessionController.getParticipants(projectId,
            taskId, sessionId);
    }

    private void validateParticipant(Map<UUID, String> participants, UUID participantUUID) {
        if (!participants.containsKey(participantUUID)) {
            throw new NoSuchElementException("Participant not found");
        }
    }

    private void validateRole(String role) {
        if (!role.equals(ParticipantRole.OBSERVER.name())
            && !role.equals(ParticipantRole.HANDLER.name())
            && !role.equals(ParticipantRole.INITIATOR.name())) {
            throw new BadRequestException("Role must be provided or Invalid role");
        }
    }

    private void updateParticipantRole(String projectId, String sessionId, String taskId, String defectId,
        String participantId, String role) {
        if (taskId == null || taskId.isBlank()) {
            chatSessionController
                .updateParticipantRole(projectId, defectId, sessionId, participantId,
                    ParticipantRole.valueOf(role));
        } else {
            chatSessionController
                .updateParticipantRole(projectId, taskId, sessionId, participantId,
                    ParticipantRole.valueOf(role));
        }
    }

    private void validateRoleUpdate(String projectId, String sessionId, String taskId, String defectId,
        String participantId, String role) {
        boolean roleUpdated = (taskId != null
            && chatSessionController
                .getParticipantRole(projectId, taskId, sessionId, participantId).equals(role))
            || (defectId != null
                && chatSessionController
                    .getParticipantRole(projectId, defectId, sessionId, participantId).equals(role));

        if (!roleUpdated) {
            throw new InternalServerErrorException("Failed to change participant role");
        }
    }

}
