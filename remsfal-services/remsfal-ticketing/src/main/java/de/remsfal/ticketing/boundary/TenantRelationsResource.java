package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import de.remsfal.core.api.ticketing.TenantRelationsEndpoint;
import de.remsfal.core.api.ticketing.TenantTimelineEndpoint;

@Authenticated
@RequestScoped
public class TenantRelationsResource extends AbstractTicketingResource implements TenantRelationsEndpoint {

    @Inject
    Instance<TenantTimelineResource> tenantTimelineResource;

    @Override
    public TenantTimelineEndpoint getTenantTimelineResource() {
        return resourceContext.initResource(tenantTimelineResource.get());
    }
}
