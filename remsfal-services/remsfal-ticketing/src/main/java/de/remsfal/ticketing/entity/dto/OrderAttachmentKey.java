package de.remsfal.ticketing.entity.dto;

import java.util.Objects;
import java.util.UUID;

import jakarta.nosql.Embeddable;
import jakarta.nosql.Id;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Embeddable
public class OrderAttachmentKey {

    @Id("process_phase")
    private String processPhase;

    @Id("process_id")
    private UUID processId;

    @Id("attachment_id")
    private UUID attachmentId;

    public OrderAttachmentKey() {
        // Default constructor
    }

    public OrderAttachmentKey(final String processPhase, final UUID processId, final UUID attachmentId) {
        this.processPhase = processPhase;
        this.processId = processId;
        this.attachmentId = attachmentId;
    }

    public String getProcessPhase() {
        return processPhase;
    }

    public void setProcessPhase(final String processPhase) {
        this.processPhase = processPhase;
    }

    public UUID getProcessId() {
        return processId;
    }

    public void setProcessId(final UUID processId) {
        this.processId = processId;
    }

    public UUID getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(final UUID attachmentId) {
        this.attachmentId = attachmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderAttachmentKey that = (OrderAttachmentKey) o;
        return Objects.equals(processPhase, that.processPhase) &&
               Objects.equals(processId, that.processId) &&
               Objects.equals(attachmentId, that.attachmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processPhase, processId, attachmentId);
    }

}
