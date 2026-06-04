package de.remsfal.core.api.organization;

import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.organization.OrganizationListJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

/**
 * @author Miroslaw Keil [miroslaw.keil@student.htw-berlin.de]
 */
@Path(OrganizationEndpoint.CONTEXT + "/" + OrganizationEndpoint.VERSION + "/" + OrganizationEndpoint.SERVICE)
public interface OrganizationEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "organizations";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/employments")
    @Operation(description = "Retrieve a list of all organizations where the user is an employee")
    @APIResponse(responseCode = "200", description = "An organization was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "List of organizations with the requested id doesn't exist")
    OrganizationListJson getOrganizationsOfUser();

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Search organizations by name for autocomplete (min. 3 characters)")
    @APIResponse(responseCode = "200", description = "Matching organizations were successfully returned")
    @APIResponse(responseCode = "400", description = "Query parameter too short (min. 3 characters)")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    OrganizationListJson searchOrganizations(
        @Parameter(description = "Name search query (min. 3 characters)")
        @QueryParam("q") @NotBlank @Size(min = 3, max = 255) String query,
        @Parameter(description = "Maximum number of results to return")
        @QueryParam("limit") @DefaultValue("10") @NotNull @Positive @Max(10) Integer limit
    );

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve a list of all organizations")
    @APIResponse(responseCode = "200", description = "List of all organizations was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization with the requested id doesn't exist")
    OrganizationListJson getOrganizations(
        @Parameter(description = "Offset of the first contractor to return")
        @QueryParam("offset") @DefaultValue("0") @NotNull @PositiveOrZero Integer offset,
        @Parameter(description = "Maximum number of contractors to return")
        @QueryParam("limit") @DefaultValue("10") @NotNull @Positive @Max(100) Integer limit
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Creates a new organization")
    @APIResponse(
        responseCode = "201",
        description = "Organization was created successfully",
        headers = @Header(name = "Location", description = "URL of the new organization")
    )
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createOrganization(
        @Parameter(description = "Organization information")
        @Valid @ConvertGroup(to = PostValidation.class) OrganizationJson organization
    );

    @GET
    @Path("/{organizationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve information of an organization by id")
    @APIResponse(responseCode = "200", description = "An organization was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization with the requested id doesn't exist")
    OrganizationJson getOrganization(
        @Parameter(description = "Id of the organization", required = true)
        @PathParam("organizationId") @NotNull UUID organizationId
    );

    @PATCH
    @Path("/{organizationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a specific organization.")
    @APIResponse(responseCode = "200", description = "An existing organization was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization does not exist")
    OrganizationJson updateOrganization(
        @Parameter(description = "Id of the organization", required = true)
        @PathParam("organizationId") @NotNull UUID organizationId,
        @Parameter(description = "Organization information")
        @Valid @ConvertGroup(to = PatchValidation.class) OrganizationJson organization
    );

    @DELETE
    @Path("/{organizationId}")
    @Operation(summary = "Delete an existing organization.")
    @APIResponse(responseCode = "204", description = "The organization was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization does not exist")
    void deleteOrganization(
        @Parameter(description = "ID of the organization", required = true)
        @PathParam("organizationId") @NotNull UUID organizationId
    );

    @Path("{organizationId}/" + EmployeeEndpoint.SERVICE)
    EmployeeEndpoint getEmployeeEndpoint();

}
