package de.remsfal.core.api.organization;

import de.remsfal.core.json.organization.OrganizationEmployeeJson;
import de.remsfal.core.json.organization.OrganizationEmployeeListJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.UUID;

public interface EmployeeEndpoint {

    String SERVICE = "employees";

    @GET
    @Path("/{employeeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "The employee was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "The user is not an employee of this organization")
    OrganizationEmployeeJson getEmployee(
            @Parameter(description = "Id of the organization", required = true)
            @PathParam("organizationId") @NotNull UUID organizationId,
            @Parameter(description = "Id of the employee", required = true)
            @PathParam("employeeId") @NotNull UUID employeeId
    );

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "A list of all existing employees was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization does not exist")
    OrganizationEmployeeListJson getEmployees(
        @Parameter(description = "Id of the organization", required = true)
        @PathParam("organizationId") @NotNull UUID organizationId
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "add an employee to an organization")
    @APIResponse(responseCode = "200", description = "A new employee was successfully added")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    OrganizationEmployeeJson addEmployee(
        @Parameter(description = "Id of the organization", required = true)
        @PathParam("organizationId") @NotNull final UUID organizationId,
        @Parameter(description = "employee information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) final OrganizationEmployeeJson employee
    );

    @PATCH
    @Path("/{employeeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update role of an employee.")
    @APIResponse(responseCode = "200", description = "An existing employee was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization or the employee does not exist")
    OrganizationEmployeeJson updateEmployee(
        @Parameter(description = "Id of the organization", required = true)
        @PathParam("organizationId") @NotNull UUID organizationId,
        @Parameter(description = "employeeId", required = true)
        @PathParam("employeeId") @NotNull UUID employeeId,
        @Parameter(description = "employee information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) final OrganizationEmployeeJson employee
    );

    @DELETE
    @Path("/{employeeId}")
    @Operation(summary = "Delete an existing employee.")
    @APIResponse(responseCode = "204", description = "The employee was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The organization or the employee does not exist")
    void deleteEmployee(
        @Parameter(description = "Id of the organization")
        @PathParam("organizationId") @NotNull final UUID organizationId,
        @Parameter(description = "Id of the employee")
        @PathParam("employeeId") @NotNull UUID employeeId
    );
}
