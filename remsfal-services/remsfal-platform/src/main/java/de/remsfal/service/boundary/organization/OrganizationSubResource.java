package de.remsfal.service.boundary.organization;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.OrganizationEmployeeModel;
import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.service.control.OrganizationController;
import de.remsfal.core.model.OrganizationEmployeeModel.PermissionType;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import java.util.UUID;

@Authenticated
@RequestScoped
public class OrganizationSubResource {

    @Context
    protected UriInfo uri;

    @Context
    protected ResourceContext resourceContext;

    @Inject
    protected RemsfalPrincipal principal;

    @Inject
    protected OrganizationController  controller;

    public boolean checkReadPermissions(final UUID organizationId) {
        return controller.getEmployeeRole(organizationId, principal) != null;
    }

    public boolean checkWritePermissions(final UUID organizationId) {
        if (!controller.getEmployeeRole(organizationId, principal).isPrivileged(PermissionType.WRITE)) {
            System.out.println("Permissions not granted");
            throw new ForbiddenException("Inadequate user rights");
        } else {
            System.out.println("Permissions granted");
            return true;
        }
    }

    public boolean checkOwnerPermissions(final UUID organizationId) {
        if (controller.getEmployeeRole(organizationId, principal) != OrganizationEmployeeModel.EmployeeRole.OWNER) {
            throw new ForbiddenException("Owner rights are required");
        } else {
            return true;
        }
    }
}
