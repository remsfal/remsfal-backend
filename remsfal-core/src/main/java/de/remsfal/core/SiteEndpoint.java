package de.remsfal.core;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.dto.SiteJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(ProjectEndpoint.CONTEXT + "/" + ProjectEndpoint.VERSION + "/" 
 + ProjectEndpoint.SERVICE + "/{projectId}/" + PropertyEndpoint.SERVICE
 + "/{propertyId}/" + SiteEndpoint.SERVICE)
public interface SiteEndpoint {

    final static String SERVICE = "sites";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new site.")
    @APIResponse(responseCode = "201", description = "Site created successfully",
        headers = @Header(name = "Location", description = "URL of the new site"))
    Response createSite(
        @Parameter(description = "Site information", required = true) @Valid SiteJson site);

    @GET
    @Path("/{siteId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a site.")
    @APIResponse(responseCode = "404", description = "The site does not exist")
    SiteJson getSite(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the property", required = true) @PathParam("propertyId") String propertyId,
        @Parameter(description = "ID of the building", required = true) @PathParam("siteId") String siteId);

}
