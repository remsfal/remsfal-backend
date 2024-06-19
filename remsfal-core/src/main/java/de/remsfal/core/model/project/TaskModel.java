package de.remsfal.core.model.project;

import java.util.Date;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TaskModel {

    String getId();

    String getProjectId();

    String getTitle();

    public enum Status {
        PENDING,
        OPEN,
        IN_PROGRESS,
        CLOSED,
        REJECTED
    }

    Status getStatus();

    String getOwnerId();

    String getDescription();

    String getBlockedBy();

    String getRelatedTo();

    String getDuplicateOf();

    Date getCreatedAt();

    Date getModifiedAt();

}
