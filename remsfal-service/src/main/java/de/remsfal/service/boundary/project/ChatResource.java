package de.remsfal.service.boundary.project;


import java.io.InputStream;
import java.net.URI;
import java.util.*;

import de.remsfal.core.model.project.ChatMessageModel.ContentType;
import de.remsfal.service.control.FileStorageService;
import jakarta.ws.rs.core.StreamingOutput;
import org.jboss.logging.Logger;

import com.google.gson.Gson;

import de.remsfal.core.api.project.ChatEndpoint;
import de.remsfal.core.json.project.ChatMessageJson;
import de.remsfal.core.json.project.ChatSessionJson;
import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.service.control.ChatMessageController;
import de.remsfal.service.control.ChatSessionController;
import de.remsfal.service.control.TaskController;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@RequestScoped
public class ChatResource extends ProjectSubResource implements ChatEndpoint {

    @Inject
    ChatSessionController chatSessionController;

    @Inject
    ChatMessageController chatMessageController;

    @Inject
    TaskController taskController;

    @Inject
    Logger logger;

    @Inject
    FileStorageService fileStorageService;

    private final String bucketName = "remsfal-chat-files";

    @Override
    public Response createChatSession() {
        try {
            String userId = principal.getId(); // Get user ID from session
            String projectId = uri.getPathParameters().getFirst("projectId");
            String taskId = uri.getPathParameters().getFirst("taskId");
            String defectId = uri.getPathParameters().getFirst("defectId");
            checkPrivileges(projectId);
            if ((taskId == null || taskId.isBlank()) && (defectId == null || defectId.isBlank()))
                throw new BadRequestException("Task ID or defect ID must be provided");
            if (taskId != null && taskController.getTask(projectId, taskId) == null)
                throw new NotFoundException("Task does not exist");
            if (defectId != null && taskController.getDefect(projectId, defectId) == null)
                throw new NotFoundException("Defect does not exist");

            ChatSessionModel.TaskType taskType = (taskId != null && !taskId.isBlank())
                ? ChatSessionModel.TaskType.TASK : ChatSessionModel.TaskType.DEFECT;
            String associatedId = (taskId != null && !taskId.isBlank()) ? taskId : defectId;

            ChatSessionModel session = chatSessionController
                .createChatSession(projectId, associatedId, taskType, userId);
            URI location = uri.getAbsolutePathBuilder().path(session.getId()).build();
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
    public Response getChatSession(String sessionId) {
        try {
            String projectId = uri.getPathParameters().getFirst("projectId");
            checkPrivileges(projectId);
            ChatSessionModel session = chatSessionController.getChatSession(sessionId);
            return Response.ok(uri.getAbsolutePath())
                .type(MediaType.APPLICATION_JSON)
                .entity(ChatSessionJson.valueOf(session))
                .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get chat session", e);
            throw e;
        }
    }

    @Override
    public Response deleteChatSession(String sessionId) {
        try {
            String projectId = uri.getPathParameters().getFirst("projectId");
            checkPrivileges(projectId);
            chatSessionController.deleteChatSession(sessionId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete chat session", e);
            throw e;
        }
    }

    @Override
    public Response updateChatSessionStatus(String sessionId, ChatSessionModel.Status status) {
        try {
            String projectId = uri.getPathParameters().getFirst("projectId");
            checkPrivileges(projectId);
            if (status != ChatSessionModel.Status.ARCHIVED && status != ChatSessionModel.Status.OPEN
                && status != ChatSessionModel.Status.CLOSED) {
                throw new BadRequestException("Status must be provided");
            }
            chatSessionController.updateChatSessionStatus(sessionId, status);
            ChatSessionEntity updatedSession = chatSessionController.getChatSession(sessionId);
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(ChatSessionJson.valueOf(updatedSession))
                .build();
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
    public Response joinChatSession(String sessionId) {
        String userId = principal.getId(); // Get user ID from session
        checkPrivileges(uri.getPathParameters().getFirst("projectId"));
        try {
            chatSessionController.addParticipant(sessionId, userId, ChatSessionModel.ParticipantRole.OBSERVER);
            Map<String, ChatSessionModel.ParticipantRole> participants =
                chatSessionController.getChatSession(sessionId).getParticipants();
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
    public Response getParticipants(String sessionId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            Map<String, ChatSessionModel.ParticipantRole> participants =
                chatSessionController.getParticipants(sessionId);
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
    public Response getParticipant(String sessionId, String participantId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            ChatSessionEntity chatSessionEntity = chatSessionController.getChatSession(sessionId);
            Map<String, ChatSessionModel.ParticipantRole> participants = chatSessionEntity.getParticipants();
            if (participants.containsKey(participantId)) {
                String json = jsonifyParticipantsMap(Map.of(participantId, participants.get(participantId)));
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
    public Response changeParticipantRole(String sessionId, String participantId,
        ChatSessionModel.ParticipantRole role) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            ChatSessionEntity chatSessionEntity = chatSessionController.getChatSession(sessionId);
            Map<String, ChatSessionModel.ParticipantRole> participants = chatSessionEntity.getParticipants();

            if (!participants.containsKey(participantId)) {
                throw new NoSuchElementException("Participant not found");
            }

            chatSessionController.updateParticipantRole(sessionId, participantId, role);
            if (!chatSessionController.getParticipantRole(sessionId, participantId).equals(role)) {
                throw new InternalServerErrorException("Failed to change participant role");
            }
            String json = jsonifyParticipantsMap(Map.of(participantId, role));
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
    public Response removeParticipant(String sessionId, String participantId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            ChatSessionEntity chatSessionEntity = chatSessionController.getChatSession(sessionId);
            Map<String, ChatSessionModel.ParticipantRole> participants = chatSessionEntity.getParticipants();

            if (!participants.containsKey(participantId)) {
                throw new NotFoundException("Participant not found");
            }
            chatSessionController.removeParticipant(sessionId, participantId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to remove participant from chat session", e);
            throw e;
        }
    }

    @Override
    public Response sendMessage(String sessionId, ChatMessageJson message) {
        try {
            String userId = principal.getId(); // Get user ID from session
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));

            if (message.getContent() == null || message.getContent().isBlank()) {
                throw new BadRequestException("Message content cannot be null or empty");
            }

            int maxPayloadSize = 8000;
            if (message.getContent().length() > maxPayloadSize) {
                throw new BadRequestException("Payload size exceeds limit");
            }

            ChatMessageEntity entity = chatMessageController.sendChatMessage(
                sessionId, userId, message.getContentType(), message.getContent());
            URI location = uri.getAbsolutePathBuilder().path(entity.getId()).build();
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
    public Response getChatMessages(String sessionId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(chatSessionController.exportChatLogs(sessionId))
                .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get chat messages", e);
            throw e;
        }

    }



    @Override
    public Response getChatMessage(String sessionId, String messageId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            ChatMessageEntity chatMessageEntity = chatMessageController.getChatMessage(messageId);
            if (chatMessageEntity.getContentType().equals(ContentType.TEXT))
                return Response.ok()
                        .type(MediaType.APPLICATION_JSON)
                        .entity(ChatMessageJson.valueOf(chatMessageEntity))
                        .build();
            if (chatMessageEntity.getContentType().equals(ContentType.FILE)) {
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
    public Response updateChatMessage(String sessionId, String messageId, ChatMessageJson message) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            int maxPayloadSize = 8000;
            if (Objects.requireNonNull(message.getContent()).length() > maxPayloadSize) {
                throw new BadRequestException("Payload size exceeds limit");
            }
            chatMessageController.updateTextChatMessage(messageId, message.getContent());
            ChatMessageEntity updatedMessage = chatMessageController.getChatMessage(messageId);
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
    public Response deleteChatMessage(String sessionId, String messageId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            if (chatSessionController.getChatSession(sessionId) == null)
                throw new NotFoundException("Chat session not found");
            chatMessageController.deleteChatMessage(messageId);
            return Response.noContent().build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to delete message", e);
            throw e;
        }
    }


    @Override
    public Response uploadFile(String sessionId, MultipartFormDataInput input) throws Exception {
        try {
            String userId = principal.getId();
            String projectId = uri.getPathParameters().getFirst("projectId");
            checkPrivileges(projectId);

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
                            .entity("{\"message\": \"Unsupported Media Type: " + contentType + "\"}")
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
                            .sendChatMessage(sessionId, userId, ContentType.FILE, fileUrl);

                    String jsonResponse = String.format("{\"fileId\": \"%s\", \"fileUrl\": \"%s\"}",
                            fileMetadataEntity.getId(), fileUrl);

                    return Response.status(Response.Status.CREATED)
                            .entity(jsonResponse)
                            .build();
                }
            }

            throw new BadRequestException("No valid file uploaded");

        } catch (Exception e) {
            logger.error("Error during file upload", e);
            throw e;
        }
    }





    //---------------------Helper Methods---------------------

    private String jsonifyParticipantsMap(Map<String, ChatSessionModel.ParticipantRole> participants) {
        List<Map<String, String>> participantList = new ArrayList<>();
        participants.forEach((id, role) -> {
            Map<String, String> participant = new HashMap<>();
            participant.put("userId", id);
            participant.put("userRole", role.toString());
            participantList.add(participant);
        });
        Gson gson = new Gson();
        return gson.toJson(participantList);
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
        Set <String> allowedTypes = fileStorageService.getAllowedTypes();
        return allowedTypes.contains(normalizedContentType);
    }




}
