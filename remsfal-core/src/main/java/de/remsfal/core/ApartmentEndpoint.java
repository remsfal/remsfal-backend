package de.remsfal.core;

import jakarta.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.dto.ApartmentJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(ProjectEndpoint.CONTEXT + "/" + ProjectEndpoint.VERSION + "/" 
 + ProjectEndpoint.SERVICE + "/{projectId}/" + PropertyEndpoint.SERVICE
 + "/{propertyId}/" + BuildingEndpoint.SERVICE
 + "/{buildingId}/" + ApartmentEndpoint.SERVICE)
public interface ApartmentEndpoint {

    final static String SERVICE = "apartments";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new apartment.")
    @APIResponse(responseCode = "201", description = "Apartment created successfully",
        headers = @Header(name = "Location", description = "URL of the new apartment"))
    Response createApartment(
        @Parameter(description = "Apartment information", required = true) @Valid ApartmentJson apartment);

    @GET
    @Path("/{apartmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a apartment.")
    @APIResponse(responseCode = "404", description = "The apartment does not exist")
    ApartmentJson getApartment(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the property", required = true) @PathParam("propertyId") String propertyId,
        @Parameter(description = "ID of the building", required = true) @PathParam("buildingId") String buildingId,
        @Parameter(description = "ID of the apartment", required = true) @PathParam("apartmentId") String apartmentId);

}
