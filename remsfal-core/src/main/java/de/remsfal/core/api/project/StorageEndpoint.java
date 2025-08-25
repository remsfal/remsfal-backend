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
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.project.StorageJson;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface StorageEndpoint {

    String SERVICE = "storages";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new storage")
    @APIResponse(
        responseCode = "201",
        description = "A new storage was successfully createded",
        headers = @Header(name = "Location", description = "URL of the new storage"),
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema    = @Schema(implementation = StorageJson.class)
        )
    )
    Response createStorage(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the building", required = true)
        @PathParam("buildingId") String buildingId,
        @Parameter(description = "Storage information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) StorageJson storage
    );

    @GET
    @Path("/{storageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a storage.")
    @APIResponse(responseCode = "200", description = "An existing storage was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The storage does not exist")
    StorageJson getStorage(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the storage", required = true)
        @PathParam("storageId") String storageId
    );

    @PATCH
    @Path("/{storageId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a storage.")
    @APIResponse(responseCode = "200", description = "An existing storage was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The storage does not exist")
    StorageJson updateStorage(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the storage", required = true)
        @PathParam("storageId") @NotNull @UUID String storageId,
        @Parameter(description = "Storage information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) StorageJson storage
    );

    @DELETE
    @Path("/{storageId}")
    @Operation(summary = "Delete an existing storage.")
    @APIResponse(responseCode = "204", description = "The storage was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteStorage(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the storage", required = true)
        @PathParam("storageId") @NotNull @UUID String storageId
    );

}
