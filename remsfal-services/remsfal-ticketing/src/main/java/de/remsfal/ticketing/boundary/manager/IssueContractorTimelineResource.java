package de.remsfal.ticketing.boundary.manager;

import de.remsfal.core.api.ticketing.ContractorTimelineEndpoint;
import de.remsfal.core.json.ticketing.ContractorTimelineListJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.ticketing.boundary.AbstractContractorTimelineResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Authenticated
@RequestScoped
public class IssueContractorTimelineResource extends AbstractContractorTimelineResource
    implements ContractorTimelineEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public ContractorTimelineListJson getTimelineEntries(final UUID issueId) {
        checkProjectIssueAccessPermissions(issueId);
        final List<QuotationRequestEntity> requests = orderManagementController.getRequestsForQuotation(issueId);
        return super.getTimelineEntries(issueId, requests);
    }

    @Override
    public Response createTimelineEntryWithAttachments(final UUID issueId, final MultipartFormDataInput input) {
        checkProjectIssueAccessPermissions(issueId);
        return super.createTimelineEntryWithAttachments(organizationId -> {
            if (organizationId == null) {
                throw new BadRequestException("organizationId is required to address a specific contractor");
            }
            return orderManagementController.getRequestForIssueByOrganizationIds(Set.of(organizationId), issueId);
        }, UserContext.MANAGER, input);
    }

}
