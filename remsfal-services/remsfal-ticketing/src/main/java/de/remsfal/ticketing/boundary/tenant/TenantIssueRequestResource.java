package de.remsfal.ticketing.boundary.tenant;

import de.remsfal.core.api.ticketing.tenant.TenantIssueRequestEndpoint;
import de.remsfal.core.json.ticketing.IssueRequestListJson;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.IssueRequestController;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Read-only view for a tenant on the requests a contractor has sent about an issue.
 */
@Authenticated
@RequestScoped
public class TenantIssueRequestResource extends AbstractTicketingResource implements TenantIssueRequestEndpoint {

    @Inject
    IssueRequestController issueRequestController;

    @Override
    public IssueRequestListJson getRequests(final UUID issueId) {
        checkTenancyIssueAccessPermissions(issueId);
        return IssueRequestListJson.valueOf(issueRequestController.getRequestsForTenant(issueId));
    }

}
