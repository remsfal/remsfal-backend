package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.ActivityFeedModel;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity("activity_feeds")
public class ActivityFeedEntity extends AbstractEntity implements ActivityFeedModel {

    @Id
    private ActivityFeedKey key;

    @Column("project_id")
    private UUID projectId;

    @Column("project_title")
    private String projectTitle;

    @Column("activity_type")
    private String activityType;

    @Column("issue_id")
    private UUID issueId;

    @Column("issue_title")
    private String issueTitle;

    @Column("issue_type")
    private String issueType;

    @Column("issue_status")
    private String issueStatus;

    @Column("issue_priority")
    private String issuePriority;

    @Column("actor_id")
    private UUID actorId;

    @Column("actor_name")
    private String actorName;

    @Column("agreement_id")
    private UUID agreementId;

    @Column("tenant_names")
    private List<String> tenantNames;

    @Column("organization_id")
    private UUID organizationId;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("contractor_name")
    private String contractorName;

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
    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
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
    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
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
    public IssueStatus getIssueStatus() {
        return issueStatus != null ? IssueStatus.valueOf(issueStatus) : null;
    }

    public void setIssueStatus(IssueStatus issueStatus) {
        this.issueStatus = issueStatus != null ? issueStatus.name() : null;
    }

    // Setter for string issue status for Cassandra mapping
    public void setIssueStatus(String issueStatus) {
        this.issueStatus = issueStatus;
    }

    @Override
    public IssuePriority getIssuePriority() {
        return issuePriority != null ? IssuePriority.valueOf(issuePriority) : null;
    }

    public void setIssuePriority(IssuePriority issuePriority) {
        this.issuePriority = issuePriority != null ? issuePriority.name() : null;
    }

    // Setter for string issue priority for Cassandra mapping
    public void setIssuePriority(String issuePriority) {
        this.issuePriority = issuePriority;
    }

    @Override
    public UUID getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(UUID agreementId) {
        this.agreementId = agreementId;
    }

    @Override
    public List<String> getTenantNames() {
        return tenantNames;
    }

    public void setTenantNames(List<String> tenantNames) {
        this.tenantNames = tenantNames;
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
    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
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
