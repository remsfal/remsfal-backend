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
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;

/**
 * @author: Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface BuildingEndpoint {

    String SERVICE = "buildings";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new building")
    @APIResponse(
        responseCode = "201",
        description = "A new building was successfully createded",
        headers = @Header(name = "Location", description = "URL of the new building"),
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema    = @Schema(implementation = BuildingJson.class)
        )
    )
    Response createBuilding(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true)
        @PathParam("propertyId") @NotNull @UUID String propertyId,
        @Parameter(description = "Building information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) BuildingJson building
    );

    @GET
    @Path("/{buildingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a building.")
    @APIResponse(responseCode = "200", description = "An existing building was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The building does not exist")
    BuildingJson getBuilding(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the building", required = true)
        @PathParam("buildingId") @NotNull @UUID String buildingId
    );

    @PATCH
    @Path("/{buildingId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a building.")
    @APIResponse(responseCode = "200", description = "An existing building was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The building does not exist")
    BuildingJson updateBuilding(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the building", required = true)
        @PathParam("buildingId") @NotNull @UUID String buildingId,
        @Parameter(description = "Building information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) BuildingJson building
    );

    @DELETE
    @Path("/{buildingId}")
    @Operation(summary = "Delete an existing building.")
    @APIResponse(responseCode = "204",description = "The building was deleted successfully" )
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The building does not exist")
    void deleteBuilding(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the building", required = true)
        @PathParam("buildingId") @NotNull @UUID String buildingId
    );

    @Path("/{buildingId}/" + ApartmentEndpoint.SERVICE)
    ApartmentEndpoint getApartmentResource();

    @Path("/{buildingId}/" + CommercialEndpoint.SERVICE)
    CommercialEndpoint getCommercialResource();

    @Path("/{buildingId}/" + StorageEndpoint.SERVICE)
    StorageEndpoint getStorageResource();

}
