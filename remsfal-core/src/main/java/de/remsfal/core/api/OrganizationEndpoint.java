package de.remsfal.core.api;

import de.remsfal.core.api.organization.EmployeeEndpoint;
import de.remsfal.core.json.OrganizationJson;
import de.remsfal.core.json.OrganizationListJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.ConvertGroup;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.UUID;

@Path(OrganizationEndpoint.CONTEXT + "/" + OrganizationEndpoint.VERSION + "/" + OrganizationEndpoint.SERVICE)
public interface OrganizationEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "organization";

    @GET
    @Path("/{organizationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve information of an organization by id")
    @APIResponse(responseCode = "200", description = "An organization was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization with the requested id doesn't exist")
    OrganizationJson getOrganization(
        @Parameter(description = "Id of the organization", required = true)
        @PathParam("organizationId") UUID organizationId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve a list of all organizations")
    @APIResponse(responseCode = "200", description = "An organization was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization with the requested id doesn't exist")
    OrganizationListJson getOrganizations(
        @Parameter(description = "Offset of the first contractor to return")
        @QueryParam("offset") @DefaultValue("0") @NotNull @PositiveOrZero Integer offset,
        @Parameter(description = "Maximum number of contractors to return")
        @QueryParam("limit") @DefaultValue("10") @NotNull @Positive @Max(100) Integer limit
    );

    //TODO: Creating and patching organization with address doesn't work
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Creates a new organization")
    @APIResponse(
        responseCode = "201",
        description = "organization was created successfully",
        headers = @Header(name = "Location", description = "URL of the new organization")
    )
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createOrganization(
        @Parameter(description = "organization information")
        @Valid @ConvertGroup(to = PostValidation.class) OrganizationJson organization);

    @PATCH
    @Path("/{organizationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a specific organization.")
    @APIResponse(responseCode = "200", description = "An existing organization was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization does not exist")
    OrganizationJson updateOrganization(
        @Parameter(description = "id of the organization")
        @PathParam("organizationId") UUID organizationId,
        @Parameter(description = "organization information")
        @Valid @ConvertGroup(to = PatchValidation.class) OrganizationJson organization);

    @DELETE
    @Path("/{organizationId}")
    @Operation(summary = "Delete an existing organization.")
    @APIResponse(responseCode = "204", description = "The organization was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization does not exist")
    void deleteOrganization(
        @Parameter(description = "ID of the organization")
        @PathParam("organizationId") UUID organizationId
    );

    @Path("{organizationId}/" + EmployeeEndpoint.SERVICE)
    EmployeeEndpoint getEmployeeEndpoint();
}
