package de.remsfal.ticketing.boundary.contractor;

import de.remsfal.core.api.ticketing.contractor.ContractorIssueRequestEndpoint;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.json.ticketing.IssueRequestListJson;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

/**
 * Request operations for a contractor's own communication about an issue.
 * <p>
 * Persistence is not implemented yet; this stub only enforces access control, same as
 * {@link ContractorTimelineResource} does for the eligible organization check.
 */
@Authenticated
@RequestScoped
public class ContractorIssueRequestResource extends AbstractTicketingResource
    implements ContractorIssueRequestEndpoint {

    @Override
    public IssueRequestListJson getRequests(final UUID issueId) {
        resolveEligibleOrganizationIds();
        throw new WebApplicationException("Issue requests are not implemented yet", Response.Status.NOT_IMPLEMENTED);
    }

    @Override
    public IssueRequestJson createRequest(final UUID issueId, final IssueRequestJson request) {
        resolveEligibleOrganizationIds();
        throw new WebApplicationException("Issue requests are not implemented yet", Response.Status.NOT_IMPLEMENTED);
    }

}
