package de.remsfal.core.api.ticketing.tenant;

import jakarta.ws.rs.Path;

@Path(TenantRelationsEndpoint.CONTEXT + "/" + TenantRelationsEndpoint.VERSION
    + "/" + TenantRelationsEndpoint.SERVICE)
public interface TenantRelationsEndpoint {

    String CONTEXT = "ticketing";
    String VERSION = "v1";
    String SERVICE = "tenant-relations";

    @Path(TenantIssueEndpoint.SERVICE)
    TenantIssueEndpoint getTenantIssueResource();
}
