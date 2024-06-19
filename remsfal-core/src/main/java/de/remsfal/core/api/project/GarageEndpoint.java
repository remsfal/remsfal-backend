package de.remsfal.core.api.project;

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

import de.remsfal.core.api.ProjectEndpoint;
import de.remsfal.core.json.project.GarageJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(ProjectEndpoint.CONTEXT + "/" + ProjectEndpoint.VERSION + "/" 
 + ProjectEndpoint.SERVICE + "/{projectId}/" + PropertyEndpoint.SERVICE
 + "/{propertyId}/" + BuildingEndpoint.SERVICE
 + "/{buildingId}/" + GarageEndpoint.SERVICE)
public interface GarageEndpoint {

    static final String SERVICE = "garages";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new garage.")
    @APIResponse(responseCode = "201", description = "Garage created successfully",
        headers = @Header(name = "Location", description = "URL of the new garage"))
    Response createGarage(
        @Parameter(description = "Garage information", required = true) @Valid GarageJson garage);

    @GET
    @Path("/{garageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a garage.")
    @APIResponse(responseCode = "404", description = "The garage does not exist")
    GarageJson getGarage(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the property", required = true) @PathParam("propertyId") String propertyId,
        @Parameter(description = "ID of the building", required = true) @PathParam("buildingId") String buildingId,
        @Parameter(description = "ID of the garage", required = true) @PathParam("garageId") String garageId);

}
