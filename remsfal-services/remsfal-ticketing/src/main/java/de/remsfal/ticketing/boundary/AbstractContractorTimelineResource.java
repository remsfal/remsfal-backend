package de.remsfal.ticketing.boundary;

import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ContractorTimelineListJson;
import de.remsfal.core.model.ticketing.ParticipantRole;
import de.remsfal.ticketing.control.ContractorTimelineController;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.net.URI;

/**
 * Shared logic for the manager- and contractor-facing contractor-timeline endpoints. Concrete
 * subclasses implement {@code ContractorTimelineEndpoint} directly and keep their {@code @Override}
 * methods visible; each one performs its own permission check, resolves the
 * {@link QuotationRequestEntity}, and delegates to the corresponding method here. The resolved
 * entity is passed in rather than the raw {@code requestId} so these methods don't share an erased
 * signature with the interface method, which would trip Bean Validation's "parallel methods must
 * not declare parameter constraints" rule once a subclass implements both.
 */
public abstract class AbstractContractorTimelineResource extends AbstractTicketingResource {

    @Inject
    ContractorTimelineController contractorTimelineController;

    protected ContractorTimelineListJson getTimelineEntries(final QuotationRequestEntity request) {
        return ContractorTimelineListJson.valueOf(
            contractorTimelineController.getTimelineEntries(request.getRequestId()));
    }

    protected Response createTimelineEntry(final QuotationRequestEntity request,
        final ParticipantRole senderRole, final ContractorTimelineJson entry) {
        final ContractorTimelineEntity created = contractorTimelineController.createTimelineEntry(
            request.getRequestId(), request.getIssueId(), principal.getId(), principal.getName(),
            senderRole, entry);

        final URI location = uri.getAbsolutePathBuilder().path(created.getTimelineId().toString()).build();
        return Response.created(location)
            .entity(ContractorTimelineJson.valueOf(created))
            .build();
    }

}
