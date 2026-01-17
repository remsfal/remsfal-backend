package de.remsfal.ticketing.model;

import java.util.List;

/**
 * Result of a user data cleanup operation, tracking successes and failures.
 * <p>
 * This class encapsulates the outcome of a best-effort cleanup process,
 * providing statistics about successful operations and any errors encountered.
 * </p>
 */
public class CleanupResult {
    
    public final int closedIssues;
    public final int clearedRelatedTo;
    public final int clearedReporterId;
    public final int clearedCreatedBy;
    public final int removedFromSessions;
    public final int anonymizedMessages;
    public final List<String> errors;

    public CleanupResult(int closedIssues, int clearedRelatedTo, int clearedReporterId,
            int clearedCreatedBy, int removedFromSessions, int anonymizedMessages,
            List<String> errors) {
        this.closedIssues = closedIssues;
        this.clearedRelatedTo = clearedRelatedTo;
        this.clearedReporterId = clearedReporterId;
        this.clearedCreatedBy = clearedCreatedBy;
        this.removedFromSessions = removedFromSessions;
        this.anonymizedMessages = anonymizedMessages;
        this.errors = errors;
    }

    /**
     * @return true if any errors occurred during cleanup
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * @return true if at least one cleanup operation succeeded
     */
    public boolean hasPartialSuccess() {
        return closedIssues > 0 || clearedRelatedTo > 0 || clearedReporterId > 0
            || clearedCreatedBy > 0 || removedFromSessions > 0 || anonymizedMessages > 0;
    }
}
