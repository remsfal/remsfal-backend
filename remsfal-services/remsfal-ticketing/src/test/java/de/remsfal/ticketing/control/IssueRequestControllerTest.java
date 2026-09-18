package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ticketing.ImmutableIssueRequestJson;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.OrderAttachmentModel;
import de.remsfal.core.model.ticketing.OrderProcessPhase;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dao.IssueRequestRepository;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueRequestControllerTest extends AbstractTicketingTest {

    private static final UserModel CONTRACTOR_USER = TicketingTestData.userModel(
        UUID.randomUUID(), "Contractor");
    private static final UserModel TENANT_USER = TicketingTestData.userModel(
        UUID.randomUUID(), "Tenant");

    @Inject
    IssueRequestController controller;

    @Inject
    IssueRequestRepository repository;

    @Inject
    IssueRepository issueRepository;

    @Inject
    ContractorTimelineController contractorTimelineController;

    @Inject
    TenantTimelineController tenantTimelineController;

    @Inject
    OrderAttachmentController orderAttachmentController;

    @Inject
    QuotationRequestRepository quotationRequestRepository;

    private UUID createIssue(final UUID agreementId) {
        return createIssue(agreementId, true);
    }

    private UUID createIssue(final UUID agreementId, final boolean visibleToTenants) {
        final UUID issueId = UUID.randomUUID();

        final IssueKey key = new IssueKey();
        key.setProjectId(UUID.randomUUID());
        key.setIssueId(issueId);

        final IssueEntity issue = new IssueEntity();
        issue.setKey(key);
        issue.setAgreementId(agreementId);
        issue.isVisibleToTenants(visibleToTenants);

        issueRepository.insert(issue);
        return issueId;
    }

    @Test
    void testCreateRequest_persistsEntityWithGeneratedKeyAndTimestamps() {
        final UUID agreementId = UUID.randomUUID();
        final UUID issueId = createIssue(agreementId);
        final UUID organizationId = UUID.randomUUID();
        final List<UUID> attachmentIds = List.of(UUID.randomUUID());

        final IssueRequestJson request = ImmutableIssueRequestJson.builder()
            .message("Bitte um Rueckmeldung")
            .attachmentIds(attachmentIds)
            .build();

        final IssueRequestEntity created = controller.createRequest(issueId, organizationId, CONTRACTOR_USER,
            request);

        assertNotNull(created.getIssueRequestId());
        assertEquals(issueId, created.getIssueId());
        assertEquals(organizationId, created.getOrganizationId());
        assertEquals(agreementId, created.getAgreementId());
        assertEquals("Bitte um Rueckmeldung", created.getMessage());
        assertEquals(attachmentIds, created.getAttachmentIds());
        assertNotNull(created.getCreatedAt());
        assertEquals(created.getCreatedAt(), created.getModifiedAt());

        final List<IssueRequestEntity> persisted = repository.findByIssue(issueId, organizationId);
        assertEquals(1, persisted.size());
        assertEquals(created.getIssueRequestId(), persisted.get(0).getIssueRequestId());
    }

    @Test
    void testCreateRequest_writesRequestCreatedContractorTimelineEntry() {
        final UUID issueId = createIssue(UUID.randomUUID());
        final UUID organizationId = UUID.randomUUID();

        controller.createRequest(issueId, organizationId, CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Termin?").build());

        final List<ContractorTimelineEntity> timeline =
            contractorTimelineController.getTimelineEntries(issueId, organizationId);
        assertEquals(1, timeline.size());
        assertEquals(MessagePurpose.REQUEST_CREATED, timeline.get(0).getPurpose());
        assertEquals("Termin?", timeline.get(0).getMessage());
        assertEquals(UserContext.CONTRACTOR, timeline.get(0).getSenderRole());
    }

    @Test
    void testGetRequestsForContractor_returnsOnlyMatchingOrganization() {
        final UUID issueId = createIssue(UUID.randomUUID());
        final UUID organizationA = UUID.randomUUID();
        final UUID organizationB = UUID.randomUUID();

        controller.createRequest(issueId, organizationA, CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Nachricht A").build());
        controller.createRequest(issueId, organizationB, CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Nachricht B").build());

        final List<IssueRequestEntity> result = controller.getRequestsForContractor(issueId, organizationA);

        assertEquals(1, result.size());
        assertEquals("Nachricht A", result.get(0).getMessage());
    }

    @Test
    void testGetRequestsForTenant_returnsRequestsAcrossAllOrganizations() {
        final UUID issueId = createIssue(UUID.randomUUID());
        final UUID organizationA = UUID.randomUUID();
        final UUID organizationB = UUID.randomUUID();

        controller.createRequest(issueId, organizationA, CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Nachricht A").build());
        controller.createRequest(issueId, organizationB, CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Nachricht B").build());

        final List<IssueRequestEntity> result = controller.getRequestsForTenant(issueId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getOrganizationId().equals(organizationA)));
        assertTrue(result.stream().anyMatch(e -> e.getOrganizationId().equals(organizationB)));
        assertFalse(controller.getRequestsForTenant(UUID.randomUUID()).stream()
            .anyMatch(e -> e.getIssueId().equals(issueId)));
    }

    @Test
    void testAnswerRequest_deletesEntityAndWritesBothTimelines() {
        final UUID agreementId = UUID.randomUUID();
        final UUID issueId = createIssue(agreementId);
        final UUID organizationId = UUID.randomUUID();

        final IssueRequestEntity created = controller.createRequest(issueId, organizationId, CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Termin?").build());

        controller.answerRequest(issueId, created.getIssueRequestId(), TENANT_USER,
            ImmutableIssueRequestJson.builder().message("Termin bestaetigt").build());

        assertTrue(repository.findByIssueAndRequestId(issueId, created.getIssueRequestId()).isEmpty());

        final List<ContractorTimelineEntity> contractorTimeline =
            contractorTimelineController.getTimelineEntries(issueId, organizationId);
        assertEquals(2, contractorTimeline.size());
        assertTrue(contractorTimeline.stream()
            .anyMatch(e -> MessagePurpose.REQUEST_ANSWERED.equals(e.getPurpose())
                && "Termin bestaetigt".equals(e.getMessage())
                && UserContext.TENANT.equals(e.getSenderRole())));

        // The tenant timeline also holds the REQUEST_CREATED entry that was mirrored when the
        // request was created (the issue is visible to the tenant), in addition to the answer itself.
        final UUID projectId = issueRepository.findByIssueId(issueId).orElseThrow().getProjectId();
        final List<TenantTimelineEntity> tenantTimeline =
            tenantTimelineController.getTimelineEntries(agreementId, issueId, projectId);
        assertEquals(2, tenantTimeline.size());
        final TenantTimelineEntity tenantAnswerEntry = tenantTimeline.stream()
            .filter(e -> MessagePurpose.REQUEST_ANSWERED.equals(e.getPurpose()))
            .findFirst().orElseThrow();
        assertEquals("Termin bestaetigt", tenantAnswerEntry.getMessage());
    }

    @Test
    void testAnswerRequest_copiesTenantAttachmentsIntoOrderAttachmentStore() throws Exception {
        final UUID agreementId = UUID.randomUUID();
        final UUID issueId = createIssue(agreementId);
        final UUID organizationId = UUID.randomUUID();

        final IssueRequestEntity created = controller.createRequest(issueId, organizationId, CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Bitte Foto vom Schaden").build());

        final QuotationRequestEntity quotationRequest = new QuotationRequestEntity();
        quotationRequest.generateId();
        quotationRequest.setIssueId(issueId);
        quotationRequest.setOrganizationId(organizationId);
        quotationRequestRepository.insert(quotationRequest);

        final String objectName = "/issues/" + issueId + "/attachments/"
            + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1;
        uploadTestFile(TicketingTestData.ATTACHMENT_FILE_PATH_1, TicketingTestData.ATTACHMENT_FILE_TYPE_1,
            objectName);
        insertAttachment(issueId, TicketingTestData.ATTACHMENT_ID_1, TicketingTestData.ATTACHMENT_FILE_PATH_1,
            TicketingTestData.ATTACHMENT_FILE_TYPE_1, objectName, TENANT_USER.getId());

        final IssueRequestJson response = ImmutableIssueRequestJson.builder()
            .message("Hier das Foto")
            .attachmentIds(List.of(TicketingTestData.ATTACHMENT_ID_1))
            .build();

        controller.answerRequest(issueId, created.getIssueRequestId(), TENANT_USER, response);

        final List<? extends OrderAttachmentModel> orderAttachments = orderAttachmentController.getAttachments(
            OrderProcessPhase.QUOTATION_REQUEST, quotationRequest.getRequestId());
        assertEquals(1, orderAttachments.size());
        final OrderAttachmentModel copiedAttachment = orderAttachments.get(0);
        assertEquals(TicketingTestData.ATTACHMENT_FILE_PATH_1, copiedAttachment.getFileName());
        assertEquals(TicketingTestData.ATTACHMENT_FILE_TYPE_1, copiedAttachment.getContentType());
        assertEquals(TENANT_USER.getId(), copiedAttachment.getUploaderId());
        assertNotEquals(TicketingTestData.ATTACHMENT_ID_1, copiedAttachment.getAttachmentId());

        final List<ContractorTimelineEntity> contractorTimeline =
            contractorTimelineController.getTimelineEntries(issueId, organizationId);
        final ContractorTimelineEntity answerEntry = contractorTimeline.stream()
            .filter(e -> MessagePurpose.REQUEST_ANSWERED.equals(e.getPurpose())
                && UserContext.TENANT.equals(e.getSenderRole()))
            .findFirst().orElseThrow();
        assertEquals(List.of(copiedAttachment.getAttachmentId()), answerEntry.getAttachmentIds());

        final UUID projectId = issueRepository.findByIssueId(issueId).orElseThrow().getProjectId();
        final List<TenantTimelineEntity> tenantTimeline =
            tenantTimelineController.getTimelineEntries(agreementId, issueId, projectId);
        final TenantTimelineEntity tenantAnswerEntry = tenantTimeline.stream()
            .filter(e -> "Hier das Foto".equals(e.getMessage()))
            .findFirst().orElseThrow();
        assertEquals(List.of(TicketingTestData.ATTACHMENT_ID_1), tenantAnswerEntry.getAttachmentIds());
    }

    @Test
    void testAnswerRequest_unknownIssueRequestId_throwsNotFound() {
        final UUID issueId = createIssue(UUID.randomUUID());
        final IssueRequestJson response = ImmutableIssueRequestJson.builder()
            .message("Termin bestaetigt")
            .build();

        assertThrows(NotFoundException.class,
            () -> controller.answerRequest(issueId, UUID.randomUUID(), TENANT_USER, response));
    }

    @Test
    void testCreateRequest_issueNotVisibleToTenant_throwsBadRequest() {
        final UUID issueId = createIssue(UUID.randomUUID(), false);
        final IssueRequestJson request = ImmutableIssueRequestJson.builder()
            .message("Termin?")
            .build();

        assertThrows(BadRequestException.class,
            () -> controller.createRequest(issueId, UUID.randomUUID(), CONTRACTOR_USER, request));
    }

    @Test
    void testCreateRequest_issueWithoutAgreement_throwsBadRequest() {
        final UUID issueId = createIssue(null, true);
        final IssueRequestJson request = ImmutableIssueRequestJson.builder()
            .message("Termin?")
            .build();

        assertThrows(BadRequestException.class,
            () -> controller.createRequest(issueId, UUID.randomUUID(), CONTRACTOR_USER, request));
    }
}
