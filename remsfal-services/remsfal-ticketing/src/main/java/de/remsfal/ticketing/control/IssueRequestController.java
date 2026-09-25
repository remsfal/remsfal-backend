package de.remsfal.ticketing.control;

import de.remsfal.common.model.FileUploadData;
import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ImmutableContractorTimelineJson;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.OrderProcessPhase;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dao.IssueRequestRepository;
import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestKey;
import de.remsfal.ticketing.entity.dto.OrderAttachmentEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class IssueRequestController {

    private static final String ISSUE_NOT_FOUND = "Issue not found";
    private static final String ISSUE_REQUEST_NOT_FOUND = "Issue request not found";

    @Inject
    Logger logger;

    @Inject
    IssueRequestRepository issueRequestRepository;

    @Inject
    IssueRepository issueRepository;

    @Inject
    ContractorTimelineController contractorTimelineController;

    @Inject
    TenantTimelineController tenantTimelineController;

    @Inject
    AttachmentController attachmentController;

    @Inject
    OrderAttachmentController orderAttachmentController;

    @Inject
    OrderManagementController orderManagementController;

    public List<IssueRequestEntity> getRequestsForContractor(final UUID issueId, final UUID organizationId) {
        logger.infov("Retrieving issue requests (issueId={0}, organizationId={1})", issueId, organizationId);
        return issueRequestRepository.findByIssue(issueId, organizationId);
    }

    public List<IssueRequestEntity> getRequestsForTenant(final UUID issueId) {
        logger.infov("Retrieving issue requests (issueId={0})", issueId);
        return issueRequestRepository.findByIssueIdOnly(issueId);
    }

    @Transactional
    public IssueRequestEntity createRequest(final UUID issueId, final UUID organizationId,
        final UserModel sender, final IssueRequestJson request, final List<UUID> attachmentIds) {
        logger.infov("Creating issue request (issueId={0}, organizationId={1})", issueId, organizationId);

        final IssueEntity issue = issueRepository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));
        if (issue.getAgreementId() == null || !Boolean.TRUE.equals(issue.isVisibleToTenants())) {
            throw new BadRequestException("Issue is not visible to a tenant, cannot request a tenant response");
        }

        final List<UUID> copiedAttachmentIds = copyAttachmentsToIssue(
            issueId, organizationId, sender, attachmentIds);

        final IssueRequestKey key = new IssueRequestKey();
        key.setIssueId(issueId);
        key.setOrganizationId(organizationId);
        key.setIssueRequestId(UUIDv7.randomUUID());

        final IssueRequestEntity entity = new IssueRequestEntity();
        entity.setKey(key);
        entity.setAgreementId(issue.getAgreementId());
        entity.setMessage(request.getMessage());
        entity.setAttachmentIds(copiedAttachmentIds.isEmpty() ? null : copiedAttachmentIds);

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        final IssueRequestEntity inserted = issueRequestRepository.insert(entity);

        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .purpose(MessagePurpose.REQUEST_CREATED)
            .message(request.getMessage())
            .build();
        contractorTimelineController.createTimelineEntry(issueId, organizationId, sender,
            UserContext.CONTRACTOR, entry, attachmentIds);

        tenantTimelineController.createTimelineEntry(issue.getAgreementId(), issueId, issue.getProjectId(),
            sender, MessagePurpose.REQUEST_CREATED, request.getMessage(), inserted.getAttachmentIds());

        return inserted;
    }

    @Transactional
    public void deleteRequest(final UUID issueId, final UUID organizationId, final UUID issueRequestId) {
        logger.infov("Deleting issue request (issueId={0}, organizationId={1}, issueRequestId={2})",
            issueId, organizationId, issueRequestId);

        final IssueRequestKey key = new IssueRequestKey();
        key.setIssueId(issueId);
        key.setOrganizationId(organizationId);
        key.setIssueRequestId(issueRequestId);

        issueRequestRepository.findById(key)
            .orElseThrow(() -> new NotFoundException(ISSUE_REQUEST_NOT_FOUND));
        issueRequestRepository.delete(key);
    }

    @Transactional
    public void answerRequest(final UUID issueId, final UUID issueRequestId, final UserModel sender,
        final IssueRequestJson response, final List<UUID> attachmentIds) {
        logger.infov("Answering issue request (issueId={0}, issueRequestId={1})", issueId, issueRequestId);

        final IssueRequestEntity entity = issueRequestRepository.findByIssueAndRequestId(issueId, issueRequestId)
            .orElseThrow(() -> new NotFoundException(ISSUE_REQUEST_NOT_FOUND));
        final IssueEntity issue = issueRepository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));

        // Copy attachments first: this is the step with external I/O (order lookup, S3 download/upload)
        // and can fail. @Transactional has no effect against Cassandra/JNoSQL, so everything after the
        // delete below cannot be rolled back - run the fallible part first so a failure here leaves the
        // request untouched and answerable again instead of losing the answer after the point of no return.
        final List<UUID> copiedAttachmentIds = copyAttachmentsToOrder(
            issueId, entity.getOrganizationId(), sender, attachmentIds);

        issueRequestRepository.delete(entity.getKey());

        tenantTimelineController.createTimelineEntry(entity.getAgreementId(), issueId, issue.getProjectId(),
            sender, MessagePurpose.REQUEST_ANSWERED, response.getMessage(), attachmentIds);

        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .purpose(MessagePurpose.REQUEST_ANSWERED)
            .message(response.getMessage())
            .build();
        contractorTimelineController.createTimelineEntry(issueId, entity.getOrganizationId(), sender,
            UserContext.TENANT, entry, copiedAttachmentIds);
    }

    private List<UUID> copyAttachmentsToOrder(final UUID issueId, final UUID organizationId,
        final UserModel sender, final List<UUID> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return List.of();
        }

        final QuotationRequestEntity request = orderManagementController
            .getRequestForIssueByOrganizationIds(Set.of(organizationId), issueId);

        final List<IssueAttachmentEntity> sources = attachmentIds.stream()
            .map(attachmentId -> attachmentController.getAttachment(issueId, attachmentId))
            .toList();
        return orderAttachmentController.copyIssueAttachments(sender,
            OrderProcessPhase.QUOTATION_REQUEST, request.getRequestId(), sources);
    }

    private List<UUID> copyAttachmentsToIssue(final UUID issueId, final UUID organizationId,
        final UserModel sender, final List<UUID> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return List.of();
        }

        final QuotationRequestEntity request = orderManagementController
            .getRequestForIssueByOrganizationIds(Set.of(organizationId), issueId);

        final List<UUID> copiedAttachmentIds = new ArrayList<>();
        try {
            for (final UUID attachmentId : attachmentIds) {
                final OrderAttachmentEntity source = orderAttachmentController.getAttachment(
                    OrderProcessPhase.QUOTATION_REQUEST, request.getRequestId(), attachmentId);
                final InputStream inputStream = orderAttachmentController.downloadAttachment(source.getObjectName());
                final FileUploadData fileData = new FileUploadData(inputStream, source.getFileName(),
                    source.getMediaType());

                final IssueAttachmentEntity copy = attachmentController.addAttachment(sender, issueId, fileData);
                copiedAttachmentIds.add(copy.getAttachmentId());
            }
        } catch (final RuntimeException e) {
            copiedAttachmentIds.forEach(id -> attachmentController.deleteAttachment(issueId, id));
            throw e;
        }
        return copiedAttachmentIds;
    }

}
