package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ImmutableContractorTimelineJson;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.UserModel;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.entity.dao.ContractorTimelineRepository;
import de.remsfal.ticketing.entity.dao.TenantTimelineRepository;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ContractorTimelineControllerTest extends AbstractTicketingTest {

    @Inject
    ContractorTimelineController controller;

    @Inject
    ContractorTimelineRepository repository;

    @Inject
    TenantTimelineRepository tenantTimelineRepository;

    @Test
    void testCreateTimelineEntry_persistsEntity() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final UUID senderId = UUID.randomUUID();
        final UserModel sender = TicketingTestData.userModel(senderId, "Bauservice GmbH");
        final List<UUID> attachmentIds = List.of(UUID.randomUUID());

        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .purpose(MessagePurpose.MESSAGE_SENT)
            .message("Bitte um Rueckmeldung")
            .build();

        final ContractorTimelineEntity created = controller.createTimelineEntry(
            issueId, organizationId, sender,
            UserContext.CONTRACTOR, entry, attachmentIds);

        assertNotNull(created.getTimelineId());
        assertEquals(issueId, created.getIssueId());
        assertEquals(organizationId, created.getOrganizationId());
        assertEquals(senderId, created.getSenderId());
        assertEquals("Bauservice GmbH", created.getSenderName());
        assertEquals(UserContext.CONTRACTOR, created.getSenderRole());
        assertEquals(MessagePurpose.MESSAGE_SENT, created.getPurpose());
        assertEquals("Bitte um Rueckmeldung", created.getMessage());
        assertEquals(attachmentIds, created.getAttachmentIds());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getModifiedAt());

        assertTrue(repository.findById(created.getKey()).isPresent());
    }

    @Test
    void testGetTimelineEntries_returnsOnlyMatchingIssue() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();

        final ContractorTimelineEntity first = createEntity(issueId, organizationId,
            UUID.randomUUID(), "Nachricht A");
        final ContractorTimelineEntity second = createEntity(issueId, organizationId,
            UUID.randomUUID(), "Nachricht B");
        final ContractorTimelineEntity otherIssue = createEntity(UUID.randomUUID(),
            UUID.randomUUID(), UUID.randomUUID(), "Andere Nachricht");

        repository.insert(first);
        repository.insert(second);
        repository.insert(otherIssue);

        final List<ContractorTimelineEntity> entries = controller.getTimelineEntries(issueId, organizationId);

        assertEquals(2, entries.size());
        assertTrue(entries.stream().anyMatch(e -> e.getTimelineId().equals(first.getTimelineId())));
        assertTrue(entries.stream().anyMatch(e -> e.getTimelineId().equals(second.getTimelineId())));
        assertFalse(entries.stream().anyMatch(e -> e.getTimelineId().equals(otherIssue.getTimelineId())));
    }

    @Test
    void testCreateTimelineEntry_messageToTenantTrue_copiesToTenantTimeline() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        insertIssue(TicketingTestData.PROJECT_ID, issueId, TicketingTestData.ISSUE_TITLE, IssueType.TASK,
            IssueStatus.OPEN, IssuePriority.MEDIUM, TicketingTestData.USER_ID_1, TicketingTestData.AGREEMENT_ID,
            null, "Beschreibung");

        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .purpose(MessagePurpose.MESSAGE_SENT)
            .message("Termin am Montag")
            .messageToTenant(true)
            .build();

        controller.createTimelineEntry(issueId, organizationId,
            TicketingTestData.userModel(UUID.randomUUID(), "Bauservice GmbH"),
            UserContext.CONTRACTOR, entry, null);

        final List<TenantTimelineEntity> tenantEntries = tenantTimelineRepository.findByIssue(
            TicketingTestData.AGREEMENT_ID, issueId, TicketingTestData.PROJECT_ID);
        assertEquals(1, tenantEntries.size());
        assertEquals("Termin am Montag", tenantEntries.get(0).getMessage());
        assertEquals(MessagePurpose.MESSAGE_SENT, tenantEntries.get(0).getPurpose());
    }

    @Test
    void testCreateTimelineEntry_messageToTenantFalse_doesNotCopy() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        insertIssue(TicketingTestData.PROJECT_ID, issueId, TicketingTestData.ISSUE_TITLE, IssueType.TASK,
            IssueStatus.OPEN, IssuePriority.MEDIUM, TicketingTestData.USER_ID_1, TicketingTestData.AGREEMENT_ID,
            null, "Beschreibung");

        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .purpose(MessagePurpose.MESSAGE_SENT)
            .message("Nur intern")
            .build();

        controller.createTimelineEntry(issueId, organizationId,
            TicketingTestData.userModel(UUID.randomUUID(), "Bauservice GmbH"),
            UserContext.CONTRACTOR, entry, null);

        final List<TenantTimelineEntity> tenantEntries = tenantTimelineRepository.findByIssue(
            TicketingTestData.AGREEMENT_ID, issueId, TicketingTestData.PROJECT_ID);
        assertTrue(tenantEntries.isEmpty());
    }

    @Test
    void testCreateTimelineEntry_messageToTenantTrue_noAgreement_skipsCopyWithoutFailing() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        insertIssue(TicketingTestData.PROJECT_ID, issueId, TicketingTestData.ISSUE_TITLE, IssueType.TASK,
            IssueStatus.OPEN, IssuePriority.MEDIUM, TicketingTestData.USER_ID_1, null,
            null, "Beschreibung");

        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .purpose(MessagePurpose.MESSAGE_SENT)
            .message("Ohne Mietverhaeltnis")
            .messageToTenant(true)
            .build();

        final ContractorTimelineEntity created = controller.createTimelineEntry(
            issueId, organizationId, TicketingTestData.userModel(UUID.randomUUID(), "Bauservice GmbH"),
            UserContext.CONTRACTOR, entry, null);

        assertNotNull(created.getTimelineId());
        assertTrue(repository.findById(created.getKey()).isPresent());
    }

    private ContractorTimelineEntity createEntity(final UUID issueId,
        final UUID organizationId, final UUID timelineId, final String message) {
        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setIssueId(issueId);
        key.setOrganizationId(organizationId);
        key.setTimelineId(timelineId);

        final ContractorTimelineEntity entity = new ContractorTimelineEntity();
        entity.setKey(key);
        entity.setSenderId(UUID.randomUUID());
        entity.setSenderName("Tester");
        entity.setSenderRole(UserContext.CONTRACTOR);
        entity.setPurpose(MessagePurpose.MESSAGE_SENT);
        entity.setMessage(message);

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return entity;
    }

}
