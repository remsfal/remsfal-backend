package de.remsfal.core.json;

import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.ticketing.ActivityFeedJson;
import de.remsfal.core.json.ticketing.ImmutableActivityFeedJson;
import de.remsfal.core.model.ticketing.ActivityFeedModel;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityFeedJsonTest {

    @Test
    void shouldBuildAndAccessActivityFeedJson() {
        final UUID id = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        final Instant createdAt = Instant.now();

        final ActivityFeedJson json = ImmutableActivityFeedJson.builder()
            .id(id)
            .projectId(projectId)
            .projectTitle("Sunset Apartments")
            .issueId(issueId)
            .activityType(IssueEventType.ISSUE_CREATED)
            .issueTitle("Critical Bug")
            .issueType(IssueType.DEFECT)
            .issueStatus(IssueStatus.OPEN)
            .issuePriority(IssuePriority.HIGH)
            .tenantNames(List.of("Jane Tenant"))
            .contractorName("Acme Corp")
            .read(true)
            .createdAt(createdAt)
            .build();

        assertEquals(id, json.getId());
        assertEquals(projectId, json.getProjectId());
        assertEquals("Sunset Apartments", json.getProjectTitle());
        assertEquals(issueId, json.getIssueId());
        assertEquals(IssueEventType.ISSUE_CREATED, json.getActivityType());
        assertEquals("Critical Bug", json.getIssueTitle());
        assertEquals(IssueType.DEFECT, json.getIssueType());
        assertEquals(IssueStatus.OPEN, json.getIssueStatus());
        assertEquals(IssuePriority.HIGH, json.getIssuePriority());
        assertEquals(List.of("Jane Tenant"), json.getTenantNames());
        assertEquals("Acme Corp", json.getContractorName());
        assertTrue(json.isRead());
        assertEquals(createdAt, json.getCreatedAt());
    }

    @Test
    void shouldConvertFromModel() {
        final UUID id = UUID.randomUUID();
        final UUID actorId = UUID.randomUUID();
        final Instant createdAt = Instant.now();

        final ActivityFeedModel model = new TestActivityFeedModel(id, actorId, createdAt);
        final ActivityFeedJson json = ActivityFeedJson.valueOf(model);

        assertEquals(id, json.getId());
        assertEquals(actorId, json.getActorId());
        assertEquals("Chat Actor", json.getActorName());
        assertEquals(IssueEventType.CHAT_MESSAGE_CREATED, json.getActivityType());
        assertFalse(json.isRead());
        assertEquals(createdAt, json.getCreatedAt());
    }

    private static final class TestActivityFeedModel implements ActivityFeedModel {

        private final UUID id;
        private final UUID actorId;
        private final Instant createdAt;

        TestActivityFeedModel(final UUID id, final UUID actorId, final Instant createdAt) {
            this.id = id;
            this.actorId = actorId;
            this.createdAt = createdAt;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public UUID getUserId() {
            return null;
        }

        @Override
        public UUID getProjectId() {
            return null;
        }

        @Override
        public String getProjectTitle() {
            return null;
        }

        @Override
        public UUID getIssueId() {
            return null;
        }

        @Override
        public IssueEventType getActivityType() {
            return IssueEventType.CHAT_MESSAGE_CREATED;
        }

        @Override
        public String getIssueTitle() {
            return null;
        }

        @Override
        public UUID getActorId() {
            return actorId;
        }

        @Override
        public String getActorName() {
            return "Chat Actor";
        }

        @Override
        public IssueType getIssueType() {
            return null;
        }

        @Override
        public IssueStatus getIssueStatus() {
            return null;
        }

        @Override
        public IssuePriority getIssuePriority() {
            return null;
        }

        @Override
        public UUID getAgreementId() {
            return null;
        }

        @Override
        public List<String> getTenantNames() {
            return null;
        }

        @Override
        public UUID getOrganizationId() {
            return null;
        }

        @Override
        public UUID getContractorId() {
            return null;
        }

        @Override
        public String getContractorName() {
            return null;
        }

        @Override
        public boolean isRead() {
            return false;
        }

        @Override
        public Instant getCreatedAt() {
            return createdAt;
        }
    }
}
