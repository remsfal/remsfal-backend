package de.remsfal.service.entity.dto;

import java.util.Objects;

import de.remsfal.core.model.project.TaskModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "TASK")
public class TaskEntity extends AbstractEntity implements TaskModel {

    public enum TaskType {
        TASK,
        DEFECT
    }

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, updatable=false, length = 36)
    private String projectId;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "OWNER_ID", columnDefinition = "char", length = 36)
    private String ownerId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "BLOCKED_BY", columnDefinition = "char", length = 36)
    private String blockedBy;

    @Column(name = "RELATED_TO", columnDefinition = "char", length = 36)
    private String relatedTo;

    @Column(name = "DUPLICATE_OF", columnDefinition = "char", length = 36)
    private String duplicateOf;

    @Column(name = "CREATED_BY", columnDefinition = "char", length = 36)
    private String createdBy;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(final TaskType type) {
        this.type = type;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(final String blockedBy) {
        this.blockedBy = blockedBy;
    }

    @Override
    public String getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(final String relatedTo) {
        this.relatedTo = relatedTo;
    }

    @Override
    public String getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(final String duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!(o instanceof TaskEntity)) {
            return false;
        }
        final TaskEntity entity = (TaskEntity) o;
        return Objects.equals(id, entity.id) &&
            Objects.equals(type, entity.type) &&
            Objects.equals(projectId, entity.projectId) &&
            Objects.equals(title, entity.title) &&
            Objects.equals(status, entity.status) &&
            Objects.equals(ownerId, entity.ownerId) &&
            Objects.equals(description, entity.description) &&
            Objects.equals(blockedBy, entity.blockedBy) &&
            Objects.equals(relatedTo, entity.relatedTo) &&
            Objects.equals(duplicateOf, entity.duplicateOf) &&
            Objects.equals(createdBy, entity.createdBy);
    }

}
