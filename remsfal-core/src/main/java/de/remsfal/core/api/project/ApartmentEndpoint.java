package de.remsfal.core.api.project;

import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.UUID;
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

import de.remsfal.core.api.ProjectEndpoint;
import de.remsfal.core.json.project.ApartmentJson;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(ProjectEndpoint.CONTEXT + "/" + ProjectEndpoint.VERSION + "/"
    + ProjectEndpoint.SERVICE + "/{projectId}/" + PropertyEndpoint.SERVICE
    + "/{propertyId}/" + BuildingEndpoint.SERVICE
    + "/{buildingId}/" + ApartmentEndpoint.SERVICE)
public interface ApartmentEndpoint {

    String SERVICE = "apartments";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new apartment.")
    @APIResponse(responseCode = "201", description = "Apartment created successfully",
        headers = @Header(name = "Location", description = "URL of the new apartment"))
    Response createApartment(
            @Parameter(description = "ID of the project", required = true)
            @PathParam("projectId") @NotNull @UUID String projectId,
            @Parameter(description = "ID of the building", required = true)
            @PathParam("buildingId") @NotNull @UUID String buildingId,
            @Parameter(description = "Apartment information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) ApartmentJson apartment
    );

    @GET
    @Path("/{apartmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of an apartment.")
    @APIResponse(responseCode = "404", description = "The apartment does not exist")
    @APIResponse(
            responseCode = "401",
            description = "No user authentication provided via session cookie"
    )
    ApartmentJson getApartment(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the building", required = true)
        @PathParam("buildingId") @NotNull @UUID String buildingId,
        @Parameter(description = "ID of the apartment", required = true)
        @PathParam("apartmentId") @NotNull @UUID String apartmentId
    );

    @PATCH
    @Path("/{apartmentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information on an apartment")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The apartment does not exist")
    ApartmentJson updateApartment(
            @Parameter(description = "ID of the project", required = true)
            @PathParam("projectId") @NotNull @UUID String projectId,
            @Parameter(description = "ID of the building", required = true)
            @PathParam("buildingId") @NotNull @UUID String buildingId,
            @Parameter(description = "ID of the apartment", required = true)
            @PathParam("apartmentId") @NotNull @UUID String apartmentId,
            @Parameter(description = "Apartment object with information", required = true)
            @Valid @ConvertGroup(to = PatchValidation.class) ApartmentJson apartment
    );

    @DELETE
    @Path("/{apartmentId}")
    @Operation(summary = "Delete an existing apartment")
    @APIResponse(responseCode = "204", description = "The building was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteApartment(
            @Parameter(description = "ID of the project", required = true)
            @PathParam("projectId") @NotNull @UUID String projectId,
            @Parameter(description = "ID of the building", required = true)
            @PathParam("buildingId") @NotNull @UUID String buildingId,
            @Parameter(description = "ID of the apartment", required = true)
            @PathParam("apartmentId") @NotNull @UUID String apartmentId
    );


}
