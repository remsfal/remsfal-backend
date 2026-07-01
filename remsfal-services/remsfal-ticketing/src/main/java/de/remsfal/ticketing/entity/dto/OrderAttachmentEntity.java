package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.OrderAttachmentModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import jakarta.ws.rs.core.MediaType;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity("order_attachments")
public class OrderAttachmentEntity extends AbstractEntity implements OrderAttachmentModel {

    @Id
    private OrderAttachmentKey key;

    @Column("file_name")
    private String fileName;

    @Column("content_type")
    private String contentType;

    @Column("object_name")
    private String objectName;

    @Column("uploader_id")
    private UUID uploaderId;

    @Column("uploaded_by")
    private String uploadedBy;

    public OrderAttachmentKey getKey() {
        return key;
    }

    public void setKey(OrderAttachmentKey key) {
        this.key = key;
    }

    @Override
    public OrderProcessPhase getProcessPhase() {
        return key != null && key.getProcessPhase() != null
            ? OrderProcessPhase.valueOf(key.getProcessPhase()) : null;
    }

    public void setProcessPhase(OrderProcessPhase processPhase) {
        if (this.key == null) {
            this.key = new OrderAttachmentKey();
        }
        this.key.setProcessPhase(processPhase != null ? processPhase.name() : null);
    }

    @Override
    public UUID getProcessId() {
        return key != null ? key.getProcessId() : null;
    }

    public void setProcessId(UUID processId) {
        if (this.key == null) {
            this.key = new OrderAttachmentKey();
        }
        this.key.setProcessId(processId);
    }

    @Override
    public UUID getAttachmentId() {
        return key != null ? key.getAttachmentId() : null;
    }

    public void setAttachmentId(UUID attachmentId) {
        if (this.key == null) {
            this.key = new OrderAttachmentKey();
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

    public MediaType getMediaType() {
        if (contentType == null) {
            return null;
        }
        return MediaType.valueOf(contentType);
    }

    public void setMediaType(MediaType mediaType) {
        this.contentType = mediaType.toString();
    }

    @Override
    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public UUID getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(UUID uploaderId) {
        this.uploaderId = uploaderId;
    }

    @Override
    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new OrderAttachmentKey();
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
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OrderAttachmentEntity that = (OrderAttachmentEntity) obj;
        return Objects.equals(key, that.key);
    }

}
