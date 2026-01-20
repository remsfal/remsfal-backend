package de.remsfal.service.boundary.organization;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.service.control.OrganizationController;
import de.remsfal.core.model.OrganizationEmployeeModel.PermissionType;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;

import java.util.UUID;

/**
 * @author Miroslaw Keil [miroslaw.keil@student.htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class OrganizationSubResource {

    @Context
    protected UriInfo uri;

    @Inject
    protected RemsfalPrincipal principal;

    @Inject
    protected OrganizationController  controller;

    @Inject
    Logger logger;

    public void checkReadPermissions(final UUID organizationId) {
        controller.getEmployeeRole(organizationId, principal);
    }

    public boolean checkWritePermissions(final UUID organizationId) {
        if (!controller.getEmployeeRole(organizationId, principal).isPrivileged(PermissionType.WRITE)) {
            logger.info("Permissions not granted");
            throw new ForbiddenException("Inadequate user rights");
        } else {
            logger.info("Permissions granted");
            return true;
        }
    }

    public boolean checkOwnerPermissions(final UUID organizationId) {
        if (controller.getEmployeeRole(organizationId, principal) != EmployeeRole.OWNER) {
            throw new ForbiddenException("Owner rights are required");
        } else {
            return true;
        }
    }
}
