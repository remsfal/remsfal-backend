package de.remsfal.core.api.project;

import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.project.CommercialJson;

/**
 * Endpoint for managing Commercial properties within buildings.
 */
public interface CommercialEndpoint {

    String SERVICE = "commercials";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new commercial")
    @APIResponse(
        responseCode = "201",
        description = "A new commercial was successfully createded",
        headers = @Header(name = "Location", description = "URL of the new commercial"),
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema    = @Schema(implementation = CommercialJson.class)
        )
    )
    Response createCommercial(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the building", required = true)
        @PathParam("buildingId") @NotNull @UUID String buildingId,
        @Parameter(description = "Commercial unit information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) CommercialJson commercial);

    @GET
    @Path("/{commercialId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information about a commercial")
    @APIResponse(responseCode = "200", description = "An existing commercial was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The commercial does not exist")
    CommercialJson getCommercial(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the commercial unit", required = true)
        @PathParam("commercialId") @NotNull @UUID String commercialId);

    @PATCH
    @Path("/{commercialId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a commercial unit")
    @APIResponse(responseCode = "200", description = "An existing commercial was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The commercial does not exist")
    CommercialJson updateCommercial(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the commercial unit", required = true)
        @PathParam("commercialId") @NotNull @UUID String commercialId,
        @Parameter(description = "Commercial unit object with information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) CommercialJson commercial);

    @DELETE
    @Path("/{commercialId}")
    @Operation(summary = "Delete an existing commercial unit")
    @APIResponse(responseCode = "204", description = "The commercial was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteCommercial(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the commercial unit", required = true)
        @PathParam("commercialId") @NotNull @UUID String commercialId);

}
