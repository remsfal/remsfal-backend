package de.remsfal.ticketing.boundary.tenant;

import de.remsfal.core.api.ticketing.tenant.TenantIssueRequestEndpoint;
import de.remsfal.core.json.ticketing.IssueRequestListJson;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

/**
 * Read-only view for a tenant on the requests a contractor has sent about an issue.
 * <p>
 * Persistence is not implemented yet; this stub only enforces access control, same as
 * {@link TenantTimelineResource} does for the tenancy access check.
 */
@Authenticated
@RequestScoped
public class TenantIssueRequestResource extends AbstractTicketingResource implements TenantIssueRequestEndpoint {

    @Override
    public IssueRequestListJson getRequests(final UUID issueId) {
        checkTenancyIssueAccessPermissions(issueId);
        throw new WebApplicationException("Issue requests are not implemented yet", Response.Status.NOT_IMPLEMENTED);
    }

}
