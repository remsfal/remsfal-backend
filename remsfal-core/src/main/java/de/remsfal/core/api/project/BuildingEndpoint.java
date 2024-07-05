package de.remsfal.core.api.project;

import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
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
import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(ProjectEndpoint.CONTEXT + "/" + ProjectEndpoint.VERSION + "/" 
    + ProjectEndpoint.SERVICE + "/{projectId}/" + PropertyEndpoint.SERVICE
    + "/{propertyId}/" + BuildingEndpoint.SERVICE)
public interface BuildingEndpoint {

    static final String SERVICE = "buildings";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new building.")
    @APIResponse(responseCode = "201", description = "Building created successfully",
        headers = @Header(name = "Location", description = "URL of the new building"))
    Response createBuilding(
        @Parameter(description = "Building information", required = true) @Valid @ConvertGroup(to = PostValidation.class) BuildingJson property);

    @GET
    @Path("/{buildingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a building.")
    @APIResponse(responseCode = "404", description = "The building does not exist")
    BuildingJson getBuilding(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the property", required = true) @PathParam("propertyId") String propertyId,
        @Parameter(description = "ID of the building", required = true) @PathParam("buildingId") String buildingId);

}
