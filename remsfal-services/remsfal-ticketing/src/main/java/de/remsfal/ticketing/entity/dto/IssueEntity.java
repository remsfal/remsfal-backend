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

    @Column("title")
    private String title;

    @Column("type")
    private String type;

    @Column("status")
    private String status;

    @Column("priority")
    private String priority;

    @Column("reporter_id")
    private UUID reporterId;

    @Column("tenancy_id")
    private UUID tenancyId;

    @Column("assignee_id")
    private UUID assigneeId;

    @Column("description")
    private String description;

    @Column("parent_issue_id")
    private UUID parentIssue;

    @Column("children_issue_ids")
    private Set<UUID> childrenIssues;

    @Column("related_to_issue_ids")
    private Set<UUID> relatedTo;

    @Column("duplicate_of_issue_ids")
    private Set<UUID> duplicateOf;

    @Column("blocked_by_issue_ids")
    private Set<UUID> blockedBy;

    @Column("blocks_issue_ids")
    private Set<UUID> blocks;

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
    public IssueType getType() {
        return type != null ? IssueType.valueOf(type) : null;
    }

    public void setType(IssueType type) {
        this.type = type != null ? type.name() : null;
    }

    // Setter for string type for Cassandra mapping
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public IssueStatus getStatus() {
        return status != null ? IssueStatus.valueOf(status) : null;
    }

    public void setStatus(IssueStatus status) {
        this.status = status != null ? status.name() : null;
    }

    // Setter for string status for Cassandra mapping
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public IssuePriority getPriority() {
        return priority != null ? IssuePriority.valueOf(priority) : null;
    }

    public void setPriority(IssuePriority priority) {
        this.priority = priority != null ? priority.name() : null;
    }

    // Setter for string priority for Cassandra mapping
    public void setPriority(String priority) {
        this.priority = priority;
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
    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public UUID getParentIssue() {
        return parentIssue;
    }

    public void setParentIssue(UUID parentIssue) {
        this.parentIssue = parentIssue;
    }

    @Override
    public Set<UUID> getChildrenIssues() {
        return childrenIssues;
    }

    public void setChildrenIssues(Set<UUID> childrenIssues) {
        this.childrenIssues = childrenIssues;
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
    public Set<UUID> getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(Set<UUID> blockedBy) {
        this.blockedBy = blockedBy;
    }

    @Override
    public Set<UUID> getBlocks() {
        return blocks;
    }

    public void setBlocks(Set<UUID> blocks) {
        this.blocks = blocks;
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