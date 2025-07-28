package de.remsfal.core.model.ticketing;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueModel {

    String getId();

    String getProjectId();

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

    String getReporterId();

    String getOwnerId();

    String getDescription();

    String getBlockedBy();

    String getRelatedTo();

    String getDuplicateOf();

}
