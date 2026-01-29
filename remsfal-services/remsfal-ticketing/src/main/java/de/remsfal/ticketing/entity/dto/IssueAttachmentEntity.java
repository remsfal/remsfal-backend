package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.IssueAttachmentModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.UUID;

/**
 * @author GitHub Copilot
 */
@Entity("issue_attachments")
public class IssueAttachmentEntity extends AbstractEntity implements IssueAttachmentModel {

    @Id
    private IssueAttachmentKey key;

    @Column("file_name")
    private String fileName;

    @Column("content_type")
    private String contentType;

    @Column("bucket")
    private String bucket;

    @Column("object_name")
    private String objectName;

    @Column("file_size")
    private Long fileSize;

    @Column("uploaded_by")
    private UUID uploadedBy;

    public IssueAttachmentKey getKey() {
        return key;
    }

    public void setKey(IssueAttachmentKey key) {
        this.key = key;
    }

    @Override
    public UUID getIssueId() {
        return key != null ? key.getIssueId() : null;
    }

    public void setIssueId(UUID issueId) {
        if (this.key == null) {
            this.key = new IssueAttachmentKey();
        }
        this.key.setIssueId(issueId);
    }

    @Override
    public UUID getAttachmentId() {
        return key != null ? key.getAttachmentId() : null;
    }

    public void setAttachmentId(UUID attachmentId) {
        if (this.key == null) {
            this.key = new IssueAttachmentKey();
        }
        this.key.setAttachmentId(attachmentId);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @Override
    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UUID uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new IssueAttachmentKey();
        }
        if (this.key.getAttachmentId() == null) {
            this.key.setAttachmentId(UUID.randomUUID());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IssueAttachmentEntity that = (IssueAttachmentEntity) obj;
        return Objects.equals(key, that.key);
    }

}
