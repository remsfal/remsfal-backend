package de.remsfal.ticketing.boundary.tenant;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import de.remsfal.core.api.ticketing.tenant.TenantIssueEndpoint;
import de.remsfal.core.api.ticketing.tenant.TenantRelationsEndpoint;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;

@Authenticated
@RequestScoped
public class TenantRelationsResource extends AbstractTicketingResource implements TenantRelationsEndpoint {

    @Inject
    Instance<TenantIssueResource> tenantIssueResource;

    @Override
    public TenantIssueEndpoint getTenantIssueResource() {
        return resourceContext.initResource(tenantIssueResource.get());
    }
}
