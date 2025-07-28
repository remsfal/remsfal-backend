package de.remsfal.service.entity.dto;

import java.util.Objects;

import de.remsfal.core.model.ticketing.IssueModel;
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
@Table(name = "TASK")
public class TaskEntity extends AbstractEntity implements IssueModel {

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, updatable=false, length = 36)
    private String projectId;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "REPORTER_ID", columnDefinition = "char", length = 36)
    private String reporterId;

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

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
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
    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(final String reporterId) {
        this.reporterId = reporterId;
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
