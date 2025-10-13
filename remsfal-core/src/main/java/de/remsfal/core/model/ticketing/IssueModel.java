package de.remsfal.core.model.ticketing;

import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueModel {

    UUID getId();

    UUID getProjectId();

    String getTitle();

    public enum Type {
        APPLICATION,
        TASK,
        DEFECT,
        MAINTENANCE
    }

    Type getType();

    public enum Status {
        PENDING,
        OPEN,
        IN_PROGRESS,
        CLOSED,
        REJECTED
    }

    Status getStatus();

    UUID getReporterId();

    UUID getOwnerId();

    String getDescription();

    UUID getBlockedBy();

    UUID getRelatedTo();

    UUID getDuplicateOf();

}
