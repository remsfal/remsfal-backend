package de.remsfal.service.entity.dto;

import java.util.Objects;
import java.util.UUID;

import de.remsfal.core.model.project.TaskModel;
import de.remsfal.service.entity.dto.superclass.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "tasks")
public class TaskEntity extends AbstractEntity implements TaskModel {

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "PROJECT_ID", nullable = false, updatable=false, columnDefinition = "uuid")
    private UUID projectId;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "REPORTER_ID", columnDefinition = "uuid")
    private UUID reporterId;

    @Column(name = "OWNER_ID", columnDefinition = "uuid")
    private UUID ownerId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "BLOCKED_BY", columnDefinition = "uuid")
    private UUID blockedBy;

    @Column(name = "RELATED_TO", columnDefinition = "uuid")
    private UUID relatedTo;

    @Column(name = "DUPLICATE_OF", columnDefinition = "uuid")
    private UUID duplicateOf;

    @Column(name = "CREATED_BY", columnDefinition = "uuid")
    private UUID createdBy;

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(final UUID projectId) {
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
    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(final UUID reporterId) {
        this.reporterId = reporterId;
    }

    @Override
    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final UUID ownerId) {
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
    public UUID getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(final UUID blockedBy) {
        this.blockedBy = blockedBy;
    }

    @Override
    public UUID getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(final UUID relatedTo) {
        this.relatedTo = relatedTo;
    }

    @Override
    public UUID getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(final UUID duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final UUID createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TaskEntity e) {
            return super.equals(e)
                && Objects.equals(type, e.type)
                && Objects.equals(projectId, e.projectId)
                && Objects.equals(title, e.title)
                && Objects.equals(status, e.status)
                && Objects.equals(ownerId, e.ownerId)
                && Objects.equals(description, e.description)
                && Objects.equals(blockedBy, e.blockedBy)
                && Objects.equals(relatedTo, e.relatedTo)
                && Objects.equals(duplicateOf, e.duplicateOf)
                && Objects.equals(createdBy, e.createdBy);
        }
        return false;
    }

}
