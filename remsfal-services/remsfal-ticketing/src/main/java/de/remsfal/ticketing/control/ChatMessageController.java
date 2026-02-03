package de.remsfal.ticketing.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import de.remsfal.common.model.FileUploadData;
import de.remsfal.core.json.ticketing.FileUploadJson;
import de.remsfal.core.json.ticketing.ImmutableFileUploadJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository.ContentType;
import de.remsfal.ticketing.entity.dto.ChatMessageEntity;
import de.remsfal.ticketing.entity.storage.FileStorage;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.UUID;

@ApplicationScoped
public class ChatMessageController {

    @Inject
    Logger logger;

    @Inject
    ChatMessageRepository repository;

    @Inject
    FileStorageController fileStorageController;

    @Inject
    OcrEventProducer ocrEventProducer;

    public ChatMessageEntity sendChatMessage(UUID sessionId, UUID userId, String contentType, String content) {
        logger.infov("Sending chat message (sessionId={0}, userEntity={1}, contentType={2})",
            sessionId, userId, contentType);
        return repository.sendMessage(sessionId, userId, contentType, content);
    }

    public void updateTextChatMessage(UUID sessionId, UUID messageId, String content) {
        logger.infov("Updating text chat message (messageId={0}, content={1})", messageId, content);
        repository.updateTextChatMessage(sessionId, messageId, content);
    }

    public void deleteChatMessage(UUID sessionId, UUID messageId) {
        logger.infov("Deleting chat message (messageId={0})", messageId);
        repository.deleteChatMessage(sessionId, messageId);
    }

    public ChatMessageEntity getChatMessage(UUID sessionId, UUID messageId) {
        logger.infov("Getting chat message (messageId={0})", messageId);
        return repository.findMessageById(sessionId, messageId)
            .orElseThrow(() -> new NoSuchElementException("Message not found"));
    }

    public ChatMessageEntity uploadFile(final UserModel user, final UUID sessionId, final FileUploadData fileData) {
        logger.infov("Uploading file to chat session (sessionId={0}, fileName={1})",
            sessionId, fileData.getFileName());

        String fileName = fileData.getFileName();
        UUID messageId = UUID.randomUUID();

        // Generate unique file name
        String objectFileName = generateUniqueFileName(fileName, sessionId, messageId);

        // Upload file to storage
        objectFileName = fileStorageController.uploadFile(fileData, objectFileName);

        // Create chat message entity with file URL
        ChatMessageEntity entity = repository.sendMessage(
            sessionId,
            user.getId(),
            ContentType.FILE.name(),
            objectFileName
        );

        // Send OCR request
        FileUploadJson uploadedFile = ImmutableFileUploadJson.builder()
            .sessionId(sessionId)
            .messageId(entity.getMessageId())
            .senderId(user.getId())
            .bucket(FileStorage.DEFAULT_BUCKET_NAME)
            .fileName(extractFileNameFromUrl(objectFileName))
            .build();
        ocrEventProducer.sendOcrRequest(uploadedFile);

        return entity;
    }

    public InputStream downloadFile(final UUID sessionId, final UUID messageId) {
        logger.infov("Downloading file from chat message (sessionId={0}, messageId={1})",
            sessionId, messageId);

        ChatMessageEntity entity = getChatMessage(sessionId, messageId);

        if (!ContentType.FILE.name().equals(entity.getContentType())) {
            throw new IllegalArgumentException("Message is not a file");
        }

        String objectFileName = entity.getUrl();
        return fileStorageController.downloadFile(objectFileName);
    }

    private String generateUniqueFileName(final String fileName, final UUID sessionId, final UUID messageId) {
        StringBuilder sb = new StringBuilder("/chat-sessions/");
        sb.append(sessionId.toString());
        sb.append("/files/");
        sb.append(messageId.toString());
        sb.append("/");
        sb.append(fileName);
        return sb.toString();
    }

    String extractFileNameFromUrl(final String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("File URL cannot be null or empty");
        }
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return fileUrl;
        }
        if (lastSlashIndex == fileUrl.length() - 1) {
            throw new IllegalArgumentException("Invalid file URL format: " + fileUrl);
        }
        return fileUrl.substring(lastSlashIndex + 1);
    }

}
