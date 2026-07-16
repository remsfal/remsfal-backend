package de.remsfal.core.model.ticketing;

/**
 * Identifies why a {@link TimelineModel} entry was created, so the client can render a
 * localized label instead of relying on a backend-generated free-text title.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public enum MessagePurpose {
    ISSUE_CREATED,
    MESSAGE_SENT,
    APPOINTMENT_REQUESTED,
    APPOINTMENT_SCHEDULED,
    STATUS_CHANGED
}
