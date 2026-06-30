package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Embeddable;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class OrderPlacementKey {

    @Id("issue_id")
    private UUID issueId;

    @Id("placement_id")
    private UUID placementId;

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(final UUID issueId) {
        this.issueId = issueId;
    }

    public UUID getPlacementId() {
        return placementId;
    }

    public void setPlacementId(final UUID placementId) {
        this.placementId = placementId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(issueId, placementId);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrderPlacementKey other)) {
            return false;
        }
        return Objects.equals(issueId, other.issueId)
            && Objects.equals(placementId, other.placementId);
    }

}
