package de.remsfal.ticketing.boundary.tenant;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.api.ticketing.tenant.TenantIssueRequestEndpoint;
import de.remsfal.core.json.ticketing.ImmutableIssueRequestListJson;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.json.ticketing.IssueRequestListJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.IssueRequestController;

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
 * Request operations for a tenant on the requests a contractor has sent about an issue.
 */
@Authenticated
@RequestScoped
public class TenantIssueRequestResource extends AbstractTicketingResource implements TenantIssueRequestEndpoint {

    @Inject
    IssueRequestController issueRequestController;

    @Inject
    Validator validator;

    @Override
    public IssueRequestListJson getRequests(final UUID issueId) {
        checkTenancyIssueAccessPermissions(issueId);
        return ImmutableIssueRequestListJson.builder()
            .requests(issueRequestController.getRequestsForTenant(issueId).stream()
                .map(request -> IssueRequestJson.valueOf(request)
                    .withAttachments(resolveAttachments(issueId, request.getAttachmentIds(), UserContext.TENANT)))
                .toList())
            .build();
    }

    @Override
    public void answerRequest(final UUID issueId, final UUID requestId, final MultipartFormDataInput input) {
        checkTenancyIssueAccessPermissions(issueId);

        final IssueRequestJson response = MultipartAttachmentProcessor.extractJsonPart(
            input, "response", IssueRequestJson.class);
        validateResponse(response);

        final List<UUID> attachmentIds = collectAttachmentIds(issueId, input, UserContext.TENANT);
        try {
            issueRequestController.answerRequest(issueId, requestId, principal, response,
                attachmentIds.isEmpty() ? null : attachmentIds);
        } catch (final RuntimeException e) {
            attachmentIds.forEach(id -> attachmentController.deleteAttachment(issueId, id));
            throw e;
        }
    }

    private void validateResponse(final IssueRequestJson response) {
        final Set<ConstraintViolation<IssueRequestJson>> violations = validator.validate(response);
        if (!violations.isEmpty()) {
            final String errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
            throw new BadRequestException("Invalid response data provided: " + errorMessages);
        }
    }

}
