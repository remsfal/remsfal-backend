package de.remsfal.core.api.project;

import de.remsfal.core.json.project.PropertyJson;
import de.remsfal.core.json.project.PropertyListJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * Author: Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface PropertyEndpoint {

    static final String SERVICE = "properties";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all properties.")
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    PropertyListJson getProperties(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Offset of the first property to return")
        @QueryParam("offset") @DefaultValue("0") @NotNull @PositiveOrZero Integer offset,
        @Parameter(description = "Maximum number of properties to return")
        @QueryParam("limit") @DefaultValue("10") @NotNull @Positive @Max(100) Integer limit
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new property.")
    @APIResponse(
        responseCode = "201",
        description = "Property created successfully",
        headers = @Header(name = "Location", description = "URL of the new property")
    )
    Response createProperty(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Property information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) PropertyJson property
    );

    @GET
    @Path("/{propertyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a property.")
    @APIResponse(
        responseCode = "404",
        description = "The property does not exist"
    )
    PropertyJson getProperty(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true)
        @PathParam("propertyId") @NotNull @UUID String propertyId
    );

    @PATCH
    @Path("/{propertyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a property.")
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    @APIResponse(
        responseCode = "404",
        description = "The property does not exist"
    )
    PropertyJson updateProperty(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true)
        @PathParam("propertyId") @NotNull @UUID String propertyId,
        @Parameter(description = "Property information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) PropertyJson property
    );

    @DELETE
    @Path("/{propertyId}")
    @Operation(summary = "Delete an existing property.")
    @APIResponse(
        responseCode = "204",
        description = "The property was deleted successfully"
    )
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    void deleteProperty(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the property", required = true)
        @PathParam("propertyId") @NotNull @UUID String propertyId
    );

    @Path("/{propertyId}/" + BuildingEndpoint.SERVICE)
    BuildingEndpoint getBuildingResource();

    @Path("/{propertyId}/" + SiteEndpoint.SERVICE)
    SiteEndpoint getSiteResource();
}
