package de.remsfal.ticketing.boundary.manager;

import de.remsfal.core.api.ticketing.ContractorTimelineEndpoint;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ContractorTimelineListJson;
import de.remsfal.core.model.ticketing.ParticipantRole;
import de.remsfal.ticketing.boundary.AbstractContractorTimelineResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Authenticated
@RequestScoped
public class IssueContractorTimelineResource extends AbstractContractorTimelineResource
    implements ContractorTimelineEndpoint {

    @PathParam("issueId")
    UUID issueId;

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public ContractorTimelineListJson getTimelineEntries() {
        checkProjectIssueAccessPermissions(issueId);
        final List<ContractorTimelineJson> entries = orderManagementController
            .getRequestsForQuotation(issueId).stream()
            .flatMap(request -> super.getTimelineEntries(request).getTimelines().stream())
            .sorted(Comparator.comparing(ContractorTimelineJson::getTimelineId))
            .toList();
        return ContractorTimelineListJson.valueOf(entries);
    }

    @Override
    public Response createTimelineEntryWithAttachments(final UUID organizationId,
        final MultipartFormDataInput input) {
        checkProjectIssueAccessPermissions(issueId);
        if (organizationId == null) {
            throw new BadRequestException("organizationId is required to address a specific contractor");
        }
        final QuotationRequestEntity request = orderManagementController
            .getRequestForIssueByOrganizationIds(Set.of(organizationId), issueId);
        return super.createTimelineEntryWithAttachments(request, ParticipantRole.MANAGER, input);
    }

}
