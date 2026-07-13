package de.remsfal.core.api.ticketing;

import jakarta.ws.rs.Path;

@Path(TenantRelationsEndpoint.CONTEXT + "/" + TenantRelationsEndpoint.VERSION
    + "/" + TenantRelationsEndpoint.SERVICE)
public interface TenantRelationsEndpoint {

    String CONTEXT = "ticketing";
    String VERSION = "v1";
    String SERVICE = "tenant-relations";

    @Path("issues/{issueId}/" + TenantTimelineEndpoint.SERVICE)
    TenantTimelineEndpoint getTenantTimelineResource();
}
