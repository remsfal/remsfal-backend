package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.IssueModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Entity("issues")
public class IssueEntity extends AbstractEntity implements IssueModel {

    @Id
    private IssueKey key;

    @Column("type")
    private String type;

    @Column("title")
    private String title;

    @Column("status")
    private String status;

    @Column("reporter_id")
    private UUID reporterId;

    @Column("tenancy_id")
    private UUID tenancyId;

    @Column("owner_id")
    private UUID ownerId;

    @Column("description")
    private String description;

    @Column("blocks_set")
    private Set<UUID> blocks;

    @Column("blocked_by_set")
    private Set<UUID> blockedBy;

    @Column("related_to_set")
    private Set<UUID> relatedTo;

    @Column("duplicate_of_set")
    private Set<UUID> duplicateOf;

    @Column("parent_of_set")
    private Set<UUID> parentOf;

    @Column("child_of_set")
    private Set<UUID> childOf;


    @Column("created_by")
    private UUID createdBy;

    public IssueKey getKey() {
        return key;
    }

    public void setKey(IssueKey key) {
        this.key = key;
    }

    @Override
    public UUID getId() {
        return Optional.ofNullable(key)
            .map(IssueKey::getIssueId)
            .orElse(null);
    }

    @Override
    public UUID getProjectId() {
        return Optional.ofNullable(key)
            .map(IssueKey::getProjectId)
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
    public UUID getTenancyId() {
        return tenancyId;
    }

    public void setTenancyId(UUID tenancyId) {
        this.tenancyId = tenancyId;
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
    public Set<UUID> getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(Set<UUID> blockedBy) {
        this.blockedBy = blockedBy;
    }

    @Override
    public Set<UUID> getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(Set<UUID> relatedTo) {
        this.relatedTo = relatedTo;
    }

    @Override
    public Set<UUID> getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(Set<UUID> duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    @Override
    public Set<UUID> getBlocks() {return blocks;}

    public void setBlocks(Set<UUID> blocks) {this.blocks = blocks;}

    @Override
    public Set<UUID> getParentOf() {return parentOf;}

    public void setParentOf(Set<UUID> parentOf) {this.parentOf = parentOf;}

    @Override
    public Set<UUID> getChildOf() {return childOf;}
    public void setChildOf(Set<UUID> childOf) {this.childOf = childOf;}

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new IssueKey();
        }
        if (this.key.getIssueId() == null) {
            this.key.setIssueId(UUID.randomUUID());
        }
    }

    public void setProjectId(UUID projectId) {
        if (this.key == null) {
            this.key = new IssueKey();
        }
        this.key.setProjectId(projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

}