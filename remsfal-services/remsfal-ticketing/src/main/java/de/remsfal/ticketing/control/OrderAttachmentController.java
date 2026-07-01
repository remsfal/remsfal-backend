package de.remsfal.ticketing.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.common.model.FileUploadData;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.OrderAttachmentModel;
import de.remsfal.core.model.ticketing.OrderAttachmentModel.OrderProcessPhase;
import de.remsfal.ticketing.entity.dao.OrderAttachmentRepository;
import de.remsfal.ticketing.entity.dto.OrderAttachmentEntity;
import de.remsfal.ticketing.entity.dto.OrderAttachmentKey;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class OrderAttachmentController {

    @Inject
    Logger logger;

    @Inject
    OrderAttachmentRepository attachmentRepository;

    @Inject
    FileStorageController fileStorageController;

    public OrderAttachmentEntity addAttachment(final UserModel user, final OrderProcessPhase processPhase,
        final UUID processId, final FileUploadData fileData) {
        logger.infov("Adding attachment (processPhase={0}, processId={1}, fileName={2})",
            processPhase, processId, fileData.getFileName());

        OrderAttachmentEntity attachment = new OrderAttachmentEntity();
        attachment.generateId();
        attachment.setProcessPhase(processPhase);
        attachment.setProcessId(processId);
        attachment.setFileName(fileData.getFileName());
        attachment.setMediaType(fileData.getMediaType());
        attachment.setUploaderId(user.getId());
        attachment.setUploadedBy(user.getName());
        attachment.setCreatedAt(Instant.now());

        String objectFileName = generateUniqueFileName(
            fileData.getFileName(), processPhase, processId, attachment.getAttachmentId());
        objectFileName = fileStorageController.uploadFile(fileData, objectFileName);
        attachment.setObjectName(objectFileName);

        return attachmentRepository.insert(attachment);
    }

    public List<? extends OrderAttachmentModel> getAttachments(final OrderProcessPhase processPhase,
        final UUID processId) {
        logger.infov("Retrieving attachments (processPhase={0}, processId={1})", processPhase, processId);
        return attachmentRepository.findByProcess(processPhase.name(), processId);
    }

    public OrderAttachmentEntity getAttachment(final OrderProcessPhase processPhase, final UUID processId,
        final UUID attachmentId) {
        logger.infov("Retrieving attachment (processPhase={0}, processId={1}, attachmentId={2})",
            processPhase, processId, attachmentId);
        return attachmentRepository.findById(new OrderAttachmentKey(processPhase.name(), processId, attachmentId))
            .orElseThrow(() -> new NotFoundException("Attachment not found"));
    }

    public InputStream downloadAttachment(final String objectName) {
        logger.infov("Downloading attachment from storage (objectName={0})", objectName);
        return fileStorageController.downloadFile(objectName);
    }

    public void deleteAttachment(final OrderProcessPhase processPhase, final UUID processId,
        final UUID attachmentId) {
        logger.infov("Deleting attachment (processPhase={0}, processId={1}, attachmentId={2})",
            processPhase, processId, attachmentId);

        OrderAttachmentEntity attachment = getAttachment(processPhase, processId, attachmentId);
        fileStorageController.deleteFile(attachment.getObjectName());
        attachmentRepository.delete(new OrderAttachmentKey(processPhase.name(), processId, attachmentId));
    }

    public void deleteAllAttachments(final OrderProcessPhase processPhase, final UUID processId) {
        logger.infov("Deleting all attachments (processPhase={0}, processId={1})", processPhase, processId);
        attachmentRepository.deleteByProcess(processPhase.name(), processId);
    }

    private String generateUniqueFileName(final String fileName, final OrderProcessPhase processPhase,
        final UUID processId, final UUID attachmentId) {
        StringBuilder sb = new StringBuilder("/order-management/");
        sb.append(processPhase.name().toLowerCase());
        sb.append("/");
        sb.append(processId.toString());
        sb.append("/attachments/");
        sb.append(attachmentId.toString());
        sb.append("/");
        sb.append(fileName);
        return sb.toString();
    }

}
