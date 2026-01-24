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

    UUID getReporterId();

    UUID getTenancyId();

    UUID getOwnerId();

    String getDescription();

    Set<UUID> getBlockedBy();

    Set<UUID> getRelatedTo();

    Set<UUID> getBlocks();

    Set<UUID> getDuplicateOf();

    Set<UUID> getParentOf();

    Set<UUID> getChildOf();

}
