package de.remsfal.ticketing.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.common.model.FileUploadData;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;
import de.remsfal.ticketing.entity.dao.IssueAttachmentRepository;
import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;
import de.remsfal.ticketing.entity.dto.IssueAttachmentKey;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class AttachmentController {

    @Inject
    Logger logger;

    @Inject
    IssueAttachmentRepository attachmentRepository;

    @Inject
    FileStorageController fileStorageController;

    public IssueAttachmentEntity addAttachment(
        final UserModel user, final UUID issueId, final FileUploadData fileData) {
        logger.infov("Adding attachment to issue (issueId={0}, fileName={1})",
            issueId, fileData.getFileName());

        IssueAttachmentEntity attachment = new IssueAttachmentEntity();
        attachment.generateId();
        attachment.setIssueId(issueId);
        attachment.setFileName(fileData.getFileName());
        attachment.setMediaType(fileData.getMediaType());
        attachment.setUploaderId(user.getId());
        attachment.setUploadedBy(user.getName());
        attachment.setCreatedAt(Instant.now());

        String objectFileName = generateUniqueFileName(
            fileData.getFileName(), issueId, attachment.getAttachmentId());
        objectFileName = fileStorageController.uploadFile(fileData, objectFileName);
        attachment.setObjectName(objectFileName);

        return attachmentRepository.insert(attachment);
    }

    public List<? extends IssueAttachmentModel> getAttachments(final UUID issueId) {
        logger.infov("Retrieving attachments for issue (issueId={0})", issueId);
        return attachmentRepository.findByIssueId(issueId);
    }

    public IssueAttachmentEntity getAttachment(final UUID issueId, final UUID attachmentId) {
        logger.infov("Retrieving attachment (issueId={0}, attachmentId={1})", issueId, attachmentId);
        return attachmentRepository.findById(new IssueAttachmentKey(issueId, attachmentId))
            .orElseThrow(() -> new NotFoundException("Attachment not found"));
    }

    public InputStream downloadAttachment(final String objectName) {
        logger.infov("Downloading attachment from storage (objectName={0})", objectName);
        return fileStorageController.downloadFile(objectName);
    }

    public void deleteAttachment(final UUID issueId, final UUID attachmentId) {
        logger.infov("Deleting attachment (issueId={0}, attachmentId={1})", issueId, attachmentId);

        IssueAttachmentEntity attachment = getAttachment(issueId, attachmentId);
        fileStorageController.deleteFile(attachment.getObjectName());
        attachmentRepository.delete(new IssueAttachmentKey(issueId, attachmentId));
    }

    public void deleteAllAttachments(final UUID issueId) {
        logger.infov("Deleting all attachments for issue (issueId={0})", issueId);
        attachmentRepository.deleteByIssueId(issueId);
    }

    private String generateUniqueFileName(final String fileName, final UUID issueId, final UUID attachmentId) {
        StringBuilder sb = new StringBuilder("/issues/");
        sb.append(issueId.toString());
        sb.append("/attachments/");
        sb.append(attachmentId.toString());
        sb.append("/");
        sb.append(fileName);
        return sb.toString();
    }

}
