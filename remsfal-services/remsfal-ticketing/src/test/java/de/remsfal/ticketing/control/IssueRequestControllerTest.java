package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ticketing.ImmutableIssueRequestJson;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.IssueRequestRepository;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueRequestControllerTest extends AbstractTicketingTest {

    @Inject
    IssueRequestController controller;

    @Inject
    IssueRequestRepository repository;

    @Test
    void testCreateRequest_persistsEntityWithGeneratedKeyAndTimestamps() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final List<UUID> attachmentIds = List.of(UUID.randomUUID());

        final IssueRequestJson request = ImmutableIssueRequestJson.builder()
            .message("Bitte um Rueckmeldung")
            .attachmentIds(attachmentIds)
            .build();

        final IssueRequestEntity created = controller.createRequest(issueId, organizationId, request);

        assertNotNull(created.getIssueRequestId());
        assertEquals(issueId, created.getIssueId());
        assertEquals(organizationId, created.getOrganizationId());
        assertEquals("Bitte um Rueckmeldung", created.getMessage());
        assertEquals(attachmentIds, created.getAttachmentIds());
        assertNotNull(created.getCreatedAt());
        assertEquals(created.getCreatedAt(), created.getModifiedAt());

        final List<IssueRequestEntity> persisted = repository.findByIssue(issueId, organizationId);
        assertEquals(1, persisted.size());
        assertEquals(created.getIssueRequestId(), persisted.get(0).getIssueRequestId());
    }

    @Test
    void testGetRequestsForContractor_returnsOnlyMatchingOrganization() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationA = UUID.randomUUID();
        final UUID organizationB = UUID.randomUUID();

        controller.createRequest(issueId, organizationA,
            ImmutableIssueRequestJson.builder().message("Nachricht A").build());
        controller.createRequest(issueId, organizationB,
            ImmutableIssueRequestJson.builder().message("Nachricht B").build());

        final List<IssueRequestEntity> result = controller.getRequestsForContractor(issueId, organizationA);

        assertEquals(1, result.size());
        assertEquals("Nachricht A", result.get(0).getMessage());
    }

    @Test
    void testGetRequestsForTenant_returnsRequestsAcrossAllOrganizations() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationA = UUID.randomUUID();
        final UUID organizationB = UUID.randomUUID();

        controller.createRequest(issueId, organizationA,
            ImmutableIssueRequestJson.builder().message("Nachricht A").build());
        controller.createRequest(issueId, organizationB,
            ImmutableIssueRequestJson.builder().message("Nachricht B").build());

        final List<IssueRequestEntity> result = controller.getRequestsForTenant(issueId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getOrganizationId().equals(organizationA)));
        assertTrue(result.stream().anyMatch(e -> e.getOrganizationId().equals(organizationB)));
        assertFalse(controller.getRequestsForTenant(UUID.randomUUID()).stream()
            .anyMatch(e -> e.getIssueId().equals(issueId)));
    }
}
