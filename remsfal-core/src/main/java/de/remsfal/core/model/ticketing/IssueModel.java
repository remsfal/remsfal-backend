package de.remsfal.core.model.ticketing;

import java.util.Set;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueModel {

    UUID getId();

    UUID getProjectId();

    String getTitle();

    public enum IssueType {
        APPLICATION,
        TASK,
        DEFECT,
        MAINTENANCE
    }

    IssueType getType();

    public enum IssueStatus {
        PENDING,
        OPEN,
        IN_PROGRESS,
        CLOSED,
        REJECTED
    }

    IssueStatus getStatus();

    enum IssuePriority {
        URGENT,
        HIGH,
        MEDIUM,
        LOW,
        UNCLASSIFIED
    }

    IssuePriority getPriority();

    UUID getReporterId();

    UUID getTenancyId();

    UUID getAssigneeId();

    String getDescription();

    UUID getParentIssue();

    Set<UUID> getChildrenIssues();

    Set<UUID> getRelatedTo();

    Set<UUID> getDuplicateOf();

    Set<UUID> getBlockedBy();

    Set<UUID> getBlocks();

}
