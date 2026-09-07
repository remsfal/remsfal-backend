package de.remsfal.ticketing.boundary.contractor;

import de.remsfal.core.api.ticketing.ContractorTimelineEndpoint;
import de.remsfal.core.json.ticketing.ContractorTimelineListJson;
import de.remsfal.core.model.ticketing.ParticipantRole;
import de.remsfal.ticketing.boundary.AbstractContractorTimelineResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.Set;
import java.util.UUID;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Authenticated
@RequestScoped
public class ContractorTimelineResource extends AbstractContractorTimelineResource
    implements ContractorTimelineEndpoint {

    @PathParam("issueId")
    UUID issueId;

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public ContractorTimelineListJson getTimelineEntries() {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity request =
            orderManagementController.getRequestForIssueByOrganizationIds(eligibleOrgIds, issueId);
        return super.getTimelineEntries(request);
    }

    @Override
    public Response createTimelineEntryWithAttachments(final UUID organizationId,
        final MultipartFormDataInput input) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity request =
            orderManagementController.getRequestForIssueByOrganizationIds(eligibleOrgIds, issueId);
        return super.createTimelineEntryWithAttachments(request, ParticipantRole.CONTRACTOR, input);
    }

}
