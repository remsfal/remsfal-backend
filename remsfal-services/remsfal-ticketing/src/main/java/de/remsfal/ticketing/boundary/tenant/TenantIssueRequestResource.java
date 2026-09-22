package de.remsfal.ticketing.boundary.tenant;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.api.ticketing.tenant.TenantIssueRequestEndpoint;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.json.ticketing.IssueRequestListJson;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.AttachmentController;
import de.remsfal.ticketing.control.IssueRequestController;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
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
    AttachmentController attachmentController;

    @Inject
    Validator validator;

    @Override
    public IssueRequestListJson getRequests(final UUID issueId) {
        checkTenancyIssueAccessPermissions(issueId);
        return IssueRequestListJson.valueOf(issueRequestController.getRequestsForTenant(issueId));
    }

    @Override
    public void answerRequest(final UUID issueId, final UUID issueRequestId, final MultipartFormDataInput input) {
        checkTenancyIssueAccessPermissions(issueId);

        final IssueRequestJson response = MultipartAttachmentProcessor.extractJsonPart(
            input, "response", IssueRequestJson.class);
        validateResponse(response);

        final List<UUID> attachmentIds = collectAttachmentIds(issueId, input);
        try {
            issueRequestController.answerRequest(issueId, issueRequestId, principal, response,
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

    private List<UUID> collectAttachmentIds(final UUID issueId, final MultipartFormDataInput input) {
        final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        final List<InputPart> fileParts = formDataMap.get("attachment");
        if (fileParts == null || fileParts.isEmpty()) {
            return List.of();
        }

        final List<IssueAttachmentJson> uploaded = MultipartAttachmentProcessor.processAttachmentParts(
            fileParts,
            fileData -> IssueAttachmentJson.valueOf(attachmentController.addAttachment(principal, issueId, fileData)));

        return uploaded.stream().map(IssueAttachmentJson::getAttachmentId).toList();
    }

}
