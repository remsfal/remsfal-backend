package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.ActivityFeedModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("activity_feeds")
public class ActivityFeedEntity extends AbstractEntity implements ActivityFeedModel {

    @Id
    private ActivityFeedKey key;

    @Column("project_id")
    private UUID projectId;

    @Column("issue_id")
    private UUID issueId;

    @Column("activity_type")
    private String activityType;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("link")
    private String link;

    @Column("actor_id")
    private UUID actorId;

    @Column("actor_name")
    private String actorName;

    @Column("issue_type")
    private String issueType;

    @Column("status")
    private String status;

    @Column("agreement_id")
    private UUID agreementId;

    @Column("organization_id")
    private UUID organizationId;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("assignee_id")
    private UUID assigneeId;

    // Named differently from the isRead() domain accessor below, for the same reason as
    // tenantUpdateJson/contractorUpdateJson in IssueEntity: JNoSQL Lite's annotation processor
    // picks a field's reader/writer by name match alone, and "isRead" would otherwise be mistaken
    // for both the getter and (nonexistent) "isRead(Boolean)" setter of this field.
    @Column("read")
    private Boolean readFlag;

    public ActivityFeedKey getKey() {
        return key;
    }

    public void setKey(ActivityFeedKey key) {
        this.key = key;
    }

    @Override
    public UUID getId() {
        return Optional.ofNullable(key)
            .map(ActivityFeedKey::getActivityId)
            .orElse(null);
    }

    @Override
    public UUID getUserId() {
        return Optional.ofNullable(key)
            .map(ActivityFeedKey::getUserId)
            .orElse(null);
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    @Override
    public IssueEventType getActivityType() {
        return activityType != null ? IssueEventType.valueOf(activityType) : null;
    }

    public void setActivityType(IssueEventType activityType) {
        this.activityType = activityType != null ? activityType.name() : null;
    }

    // Setter for string activity type for Cassandra mapping
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    @Override
    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    @Override
    public IssueType getIssueType() {
        return issueType != null ? IssueType.valueOf(issueType) : null;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType != null ? issueType.name() : null;
    }

    // Setter for string issue type for Cassandra mapping
    public void setIssueType(String issueType) {
        this.issueType = issueType;
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
    public UUID getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(UUID agreementId) {
        this.agreementId = agreementId;
    }

    @Override
    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(UUID contractorId) {
        this.contractorId = contractorId;
    }

    @Override
    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
    }

    @Override
    public boolean isRead() {
        return Boolean.TRUE.equals(readFlag);
    }

    public Boolean getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Boolean readFlag) {
        this.readFlag = readFlag;
    }

}
