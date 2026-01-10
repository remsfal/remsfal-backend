package de.remsfal.ticketing.boundary;

import de.remsfal.core.api.ticketing.QuotationRequestEndpoint;
import de.remsfal.core.json.UserJson.UserRole;
import de.remsfal.core.json.quotation.CreateQuotationRequestJson;
import de.remsfal.core.json.quotation.QuotationRequestListJson;
import de.remsfal.core.model.quotation.QuotationRequestModel;
import de.remsfal.ticketing.control.QuotationRequestController;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class QuotationRequestResource extends AbstractResource implements QuotationRequestEndpoint {

    @Inject
    QuotationRequestController quotationRequestController;

    @Override
    public Response createQuotationRequest(final UUID issueId, final CreateQuotationRequestJson request) {
        IssueEntity issue = issueController.getIssue(issueId);
        UserRole principalRole = getPrincipalRole(issue.getProjectId());

        if (principalRole != UserRole.MANAGER) {
            throw new ForbiddenException("Only managers can create quotation requests");
        }

        List<? extends QuotationRequestModel> quotationRequests =
            quotationRequestController.createQuotationRequests(
                principal,
                issueId,
                issue.getProjectId(),
                request.getContractorIds(),
                request.getDescription());

        QuotationRequestListJson responseJson = QuotationRequestListJson.valueOf(quotationRequests);
        return Response.status(Response.Status.CREATED)
            .type(MediaType.APPLICATION_JSON)
            .entity(responseJson)
            .build();
    }

    @Override
    public QuotationRequestListJson getQuotationRequests(final UUID issueId, final UUID contractorId,
        final UUID projectId) {
        List<QuotationRequestModel> allRequests = new ArrayList<>();

        if (issueId != null) {
            allRequests.addAll(quotationRequestController.getQuotationRequestsByIssueId(issueId));
        } else if (contractorId != null) {
            allRequests.addAll(quotationRequestController.getQuotationRequestsByContractorId(contractorId));
        } else if (projectId != null) {
            // Check if user has access to the project
            if (!principal.getProjectRoles().containsKey(projectId)) {
                throw new ForbiddenException(
                    "User does not have permission to view quotation requests for this project");
            }
            allRequests.addAll(quotationRequestController.getQuotationRequestsByProjectId(projectId));
        } else {
            // Return all quotation requests accessible to the user
            List<UUID> projectIds = new ArrayList<>(principal.getProjectRoles().keySet());
            for (UUID pid : projectIds) {
                allRequests.addAll(quotationRequestController.getQuotationRequestsByProjectId(pid));
            }
        }

        return QuotationRequestListJson.valueOf(allRequests);
    }

    @Override
    public void invalidateQuotationRequest(final UUID requestId) {
        QuotationRequestEntity entity = quotationRequestController.getQuotationRequest(requestId);

        // Check if user has permission (must be a manager of the project)
        UserRole principalRole = getPrincipalRole(entity.getProjectId());
        if (principalRole != UserRole.MANAGER) {
            throw new ForbiddenException("Only managers can invalidate quotation requests");
        }

        quotationRequestController.invalidateQuotationRequest(requestId);
    }

}
