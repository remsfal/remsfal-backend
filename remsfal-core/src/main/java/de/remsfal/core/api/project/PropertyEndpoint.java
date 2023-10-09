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

import de.remsfal.core.json.PropertyJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(ProjectEndpoint.CONTEXT + "/" + ProjectEndpoint.VERSION + "/" 
 + ProjectEndpoint.SERVICE + "/{projectId}/" + PropertyEndpoint.SERVICE)
public interface PropertyEndpoint {

    final static String SERVICE = "properties";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new property.")
    @APIResponse(responseCode = "201", description = "Property created successfully",
        headers = @Header(name = "Location", description = "URL of the new property"))
    Response createProperty(
        @Parameter(description = "Property information", required = true) @Valid PropertyJson property);

    @GET
    @Path("/{propertyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a property.")
    @APIResponse(responseCode = "404", description = "The property does not exist")
    PropertyJson getProperty(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the property", required = true) @PathParam("propertyId") String propertyId);

}
