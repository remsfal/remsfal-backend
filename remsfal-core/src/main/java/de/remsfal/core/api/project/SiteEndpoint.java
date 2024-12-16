package de.remsfal.core.api.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
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
import de.remsfal.core.json.project.SiteJson;
import de.remsfal.core.json.project.SiteListJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;

/**
 * @author: Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface SiteEndpoint {

    String SERVICE = "sites";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all sites.")
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    SiteListJson getSites(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true)
        @PathParam("propertyId") @NotNull @UUID String propertyId
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new site.")
    @APIResponse(
        responseCode = "201",
        description = "Site created successfully",
        headers = @Header(name = "Location", description = "URL of the new site")
    )
    Response createSite(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true)
        @PathParam("propertyId") @NotNull @UUID String propertyId,
        @Parameter(description = "Site information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) SiteJson site
    );

    @GET
    @Path("/{siteId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a site.")
    @APIResponse(
        responseCode = "404",
        description = "The site does not exist"
    )
    SiteJson getSite(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true)
        @PathParam("propertyId") @NotNull @UUID String propertyId,
        @Parameter(description = "ID of the site", required = true)
        @PathParam("siteId") @NotNull @UUID String siteId
    );

    @PATCH
    @Path("/{siteId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a site.")
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    @APIResponse(
        responseCode = "404",
        description = "The site does not exist"
    )
    SiteJson updateSite(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true)
        @PathParam("propertyId") @NotNull @UUID String propertyId,
        @Parameter(description = "ID of the site", required = true)
        @PathParam("siteId") @NotNull @UUID String siteId,
        @Parameter(description = "Site information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) SiteJson site
    );

    @DELETE
    @Path("/{siteId}")
    @Operation(summary = "Delete an existing site.")
    @APIResponse(
        responseCode = "204",
        description = "The site was deleted successfully"
    )
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    void deleteSite(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true)
        @PathParam("propertyId") @NotNull @UUID String propertyId,
        @Parameter(description = "ID of the site", required = true)
        @PathParam("siteId") @NotNull @UUID String siteId
    );

}
