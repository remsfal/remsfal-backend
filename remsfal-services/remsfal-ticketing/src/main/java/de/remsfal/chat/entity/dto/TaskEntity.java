package de.remsfal.chat.entity.dto;

import de.remsfal.core.model.ticketing.TaskModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Entity("tasks")
public class TaskEntity extends AbstractEntity implements TaskModel {

    @Id
    private TaskKey key;

    @Column("type")
    private String type;

    @Column("title")
    private String title;

    @Column("status")
    private String status;

    @Column("reporter_id")
    private UUID reporterId;

    @Column("owner_id")
    private UUID ownerId;

    @Column("description")
    private String description;

    @Column("blocked_by")
    private UUID blockedBy;

    @Column("related_to")
    private UUID relatedTo;

    @Column("duplicate_of")
    private UUID duplicateOf;

    @Column("created_by")
    private UUID createdBy;

    public TaskKey getKey() {
        return key;
    }

    public void setKey(TaskKey key) {
        this.key = key;
    }

    @Override
    public UUID getId() {
        return Optional.ofNullable(key)
            .map(TaskKey::getId)
            .orElse(null);
    }

    @Override
    public UUID getProjectId() {
        return Optional.ofNullable(key)
            .map(TaskKey::getProjectId)
            .orElse(null);
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Type getType() {
        return type != null ? Type.valueOf(type) : null;
    }

    public void setType(Type type) {
        this.type = type != null ? type.name() : null;
    }
    
    // Setter for string type for Cassandra mapping
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Status getStatus() {
        return status != null ? Status.valueOf(status) : null;
    }

    public void setStatus(Status status) {
        this.status = status != null ? status.name() : null;
    }
    
    // Setter for string status for Cassandra mapping  
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(UUID reporterId) {
        this.reporterId = reporterId;
    }

    @Override
    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public UUID getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(UUID blockedBy) {
        this.blockedBy = blockedBy;
    }

    @Override
    public UUID getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(UUID relatedTo) {
        this.relatedTo = relatedTo;
    }

    @Override
    public UUID getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(UUID duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new TaskKey();
        }
        if (this.key.getId() == null) {
            this.key.setId(UUID.randomUUID());
        }
    }

    public void setProjectId(UUID projectId) {
        if (this.key == null) {
            this.key = new TaskKey();
        }
        this.key.setProjectId(projectId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskEntity that = (TaskEntity) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "key=" + key +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", ownerId=" + ownerId +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                '}';
    }
}