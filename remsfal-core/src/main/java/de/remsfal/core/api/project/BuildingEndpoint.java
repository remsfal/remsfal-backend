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

import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface BuildingEndpoint {

    static final String SERVICE = "buildings";


    @POST
    @Path("/{projectId}/{propertyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new building.")
    @APIResponse(responseCode = "201", description = "Building created successfully",
        headers = @Header(name = "Location", description = "URL of the new building"))
    Response createBuilding(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true) @PathParam("propertyId") @NotNull @UUID String propertyId,
        @Parameter(description = "Building information", required = true) @Valid @ConvertGroup(to = PostValidation.class) BuildingJson building);

    @GET
    @Path("/{projectId}/{buildingId}/{propertyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a building.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The building does not exist")
    BuildingJson getBuilding(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the building", required = true) @PathParam("buildingId") @NotNull @UUID String buildingId,
        @Parameter(description = "ID of the property", required = true) @PathParam("propertyId") @NotNull @UUID String propertyId);

    @PATCH
    @Path("/{buildingId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a building.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The building does not exist")
    BuildingJson updateBuilding(
        @Parameter(description = "ID of the property", required = true) @PathParam("propertyId") @NotNull @UUID String propertyId,
        @Parameter(description = "ID of the building", required = true) @PathParam("buildingId") @NotNull @UUID String buildingId,
        @Parameter(description = "Property information", required = true) @Valid @ConvertGroup(to = PatchValidation.class) BuildingJson building);

    @DELETE
    @Path("/{buildingId}")
    @Operation(summary = "Delete an existing building.")
    @APIResponse(responseCode = "204", description = "The building was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteBuilding(
        @Parameter(description = "ID of the property", required = true) @PathParam("propertyId") @NotNull @UUID String propertyId,
        @Parameter(description = "ID of the building", required = true) @PathParam("buildingId") @NotNull @UUID String buildingId);

}
