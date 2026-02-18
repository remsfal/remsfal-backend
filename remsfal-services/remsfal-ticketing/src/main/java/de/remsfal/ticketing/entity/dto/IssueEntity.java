package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.HashSet;
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

    @Column("category")
    private String category;

    @Column("status")
    private String status;

    @Column("priority")
    private String priority;

    @Column("reporter_id")
    private UUID reporterId;

    @Column("agreement_id")
    private UUID agreementId;

    @Column("is_visable_to_tenants")
    private Boolean visibleToTenants;

    @Column("rental_unit_id")
    private UUID rentalUnitId;

    @Column("rental_unit_type")
    private String rentalUnitType;

    @Column("assignee_id")
    private UUID assigneeId;

    @Column("location")
    private String location;

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
    public IssueCategory getCategory() {
        return category != null ? IssueCategory.valueOf(category) : null;
    }

    public void setCategory(IssueStatus category) {
        this.category = category != null ? category.name() : null;
    }

    // Setter for string category for Cassandra mapping
    public void setCategory(String category) {
        this.category = category;
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
    public UUID getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(UUID agreementId) {
        this.agreementId = agreementId;
    }

    @Override
    public Boolean isVisibleToTenants() {
        return visibleToTenants;
    }

    // Required by JNoSQL Lite for field read access
    public Boolean getVisibleToTenants() {
        return visibleToTenants;
    }

    public void setVisibleToTenants(Boolean visibleToTenants) {
        this.visibleToTenants = visibleToTenants;
    }

    // Required by JNoSQL Lite for field write access
    public void isVisibleToTenants(Boolean visibleToTenants) {
        this.visibleToTenants = visibleToTenants;
    }

    @Override
    public UUID getRentalUnitId() {
        return rentalUnitId;
    }

    public void setRentalUnitId(UUID rentalUnitId) {
        this.rentalUnitId = rentalUnitId;
    }

    @Override
    public UnitType getRentalUnitType() {
        return rentalUnitType != null ? UnitType.valueOf(rentalUnitType) : null;
    }

    public void setRentalUnitType(UnitType rentalUnitType) {
        this.rentalUnitType = rentalUnitType != null ? rentalUnitType.name() : null;
    }

    // Setter for string rentalUnitType for Cassandra mapping
    public void setRentalUnitType(String rentalUnitType) {
        this.rentalUnitType = rentalUnitType;
    }

    @Override
    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public void addChildrenIssue(UUID childrenIssue) {
        if (this.childrenIssues == null) {
            this.childrenIssues = new HashSet<>();
        }
        this.childrenIssues.add(childrenIssue);
    }

    @Override
    public Set<UUID> getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(Set<UUID> relatedTo) {
        this.relatedTo = relatedTo;
    }

    public void addRelatedTo(UUID relatedIssue) {
        if (this.relatedTo == null) {
            this.relatedTo = new HashSet<>();
        }
        this.relatedTo.add(relatedIssue);
    }

    @Override
    public Set<UUID> getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(Set<UUID> duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public void addDuplicateOf(UUID duplicateIssue) {
        if (this.duplicateOf == null) {
            this.duplicateOf = new HashSet<>();
        }
        this.duplicateOf.add(duplicateIssue);
    }

    @Override
    public Set<UUID> getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(Set<UUID> blockedBy) {
        this.blockedBy = blockedBy;
    }

    public void addBlockedBy(UUID blockedByIssue) {
        if (this.blockedBy == null) {
            this.blockedBy = new HashSet<>();
        }
        this.blockedBy.add(blockedByIssue);
    }

    @Override
    public Set<UUID> getBlocks() {
        return blocks;
    }

    public void setBlocks(Set<UUID> blocks) {
        this.blocks = blocks;
    }

    public void addBlocks(UUID blocksIssue) {
        if (this.blocks == null) {
            this.blocks = new HashSet<>();
        }
        this.blocks.add(blocksIssue);
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