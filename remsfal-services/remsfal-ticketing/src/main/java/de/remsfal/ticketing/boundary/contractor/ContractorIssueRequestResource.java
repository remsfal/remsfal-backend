package de.remsfal.ticketing.boundary.contractor;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.api.ticketing.contractor.ContractorIssueRequestEndpoint;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.json.ticketing.IssueRequestListJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.IssueRequestController;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Request operations for a contractor's own communication about an issue.
 */
@Authenticated
@RequestScoped
public class ContractorIssueRequestResource extends AbstractTicketingResource
    implements ContractorIssueRequestEndpoint {

    @Inject
    IssueRequestController issueRequestController;

    @Inject
    OrderManagementController orderManagementController;

    @Inject
    Validator validator;

    @Override
    public IssueRequestListJson getRequests(final UUID issueId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity request =
            orderManagementController.getRequestForIssueByOrganizationIds(eligibleOrgIds, issueId);
        return IssueRequestListJson.valueOf(
            issueRequestController.getRequestsForContractor(issueId, request.getOrganizationId()));
    }

    @Override
    public IssueRequestJson createRequest(final UUID issueId, final MultipartFormDataInput input) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity quotationRequest =
            orderManagementController.getRequestForIssueByOrganizationIds(eligibleOrgIds, issueId);

        final IssueRequestJson request = MultipartAttachmentProcessor.extractJsonPart(
            input, "request", IssueRequestJson.class);
        validateRequest(request);

        final List<UUID> attachmentIds = collectAttachmentIds(issueId, input, UserContext.CONTRACTOR);
        try {
            final IssueRequestEntity created = issueRequestController.createRequest(issueId,
                quotationRequest.getOrganizationId(), principal, request,
                attachmentIds.isEmpty() ? null : attachmentIds);
            return IssueRequestJson.valueOf(created);
        } catch (final RuntimeException e) {
            attachmentIds.forEach(id -> attachmentController.deleteAttachment(issueId, id));
            throw e;
        }
    }

    @Override
    public void deleteRequest(final UUID issueId, final UUID requestId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity quotationRequest =
            orderManagementController.getRequestForIssueByOrganizationIds(eligibleOrgIds, issueId);

        issueRequestController.deleteRequest(issueId, quotationRequest.getOrganizationId(), requestId);
    }

    private void validateRequest(final IssueRequestJson request) {
        final Set<ConstraintViolation<IssueRequestJson>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            final String errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
            throw new BadRequestException("Invalid request data provided: " + errorMessages);
        }
    }

}
