package de.remsfal.core.api.project;

import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.project.StorageJson;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface StorageEndpoint {

    String SERVICE = "garages";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new garage.")
    @APIResponse(
        responseCode = "201",
        description = "Garage created successfully",
        headers = @Header(name = "Location", description = "URL of the new garage"))
    Response createGarage(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the building", required = true)
        @PathParam("buildingId") String buildingId,
        @Parameter(description = "Garage information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) StorageJson garage
    );

    @GET
    @Path("/{garageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a garage.")
    @APIResponse(
        responseCode = "404",
        description = "The garage does not exist")
    StorageJson getGarage(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the garage", required = true)
        @PathParam("garageId") String garageId
    );

    @PATCH
    @Path("/{garageId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a garage.")
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    @APIResponse(
        responseCode = "404",
        description = "The garage does not exist"
    )
    StorageJson updateGarage(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the garage", required = true)
        @PathParam("garageId") @NotNull @UUID String garageId,
        @Parameter(description = "Garage information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) StorageJson garage
    );

    @DELETE
    @Path("/{garageId}")
    @Operation(summary = "Delete an existing garage.")
    @APIResponse(
        responseCode = "204",
        description = "The garage was deleted successfully"
    )
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    void deleteGarage(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the garage", required = true)
        @PathParam("garageId") @NotNull @UUID String garageId
    );

}
