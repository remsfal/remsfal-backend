package de.remsfal.service.boundary.organization;

import de.remsfal.core.api.organization.EmployeeEndpoint;
import de.remsfal.core.json.organization.OrganizationEmployeeJson;
import de.remsfal.core.json.organization.OrganizationEmployeeListJson;
import de.remsfal.core.model.OrganizationEmployeeModel;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;

import java.util.UUID;

/**
 * @author Miroslaw Keil [miroslaw.keil@student.htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class EmployeeResource extends OrganizationSubResource implements EmployeeEndpoint {

    @Override
    public OrganizationEmployeeJson getEmployee(final UUID organizationId, final UUID employeeId) {
        checkReadPermissions(organizationId);
        return OrganizationEmployeeJson.valueOf(controller.getOrganizationEmployee(organizationId, employeeId));
    }

    @Override
    public OrganizationEmployeeListJson getEmployees(final UUID organizationId) {
        checkReadPermissions(organizationId);
        return OrganizationEmployeeListJson.valueOfList(controller.getEmployeesByOrganization(organizationId));
    }

    @Override
    public OrganizationEmployeeJson addEmployee(final UUID organizationId, final OrganizationEmployeeJson employee) {
        if (employee.getEmployeeRole() == EmployeeRole.OWNER) {
            checkOwnerPermissions(organizationId);
        } else {
            checkWritePermissions(organizationId);
        }
        return OrganizationEmployeeJson.valueOf(controller.addEmployee(organizationId, principal, employee));
    }

    @Override
    public OrganizationEmployeeJson updateEmployee(final UUID organizationId, final UUID employeeId,
        OrganizationEmployeeJson employee) {
        if (employee.getEmployeeRole() == EmployeeRole.OWNER) {
            checkOwnerPermissions(organizationId);
        } else {
            checkWritePermissions(organizationId);
        }
        return OrganizationEmployeeJson.valueOf(
                controller.updateEmployeeRole(organizationId, employeeId, employee.getEmployeeRole())
        );
    }

    @Override
    public void deleteEmployee(final UUID organizationId, final UUID employeeId) {
        EmployeeRole role =
            controller.getOrganizationEmployee(organizationId, employeeId).getEmployeeRole();

        if (role == EmployeeRole.OWNER) {
            checkOwnerPermissions(organizationId);
        } else {
            checkWritePermissions(organizationId);
        }

        boolean targetHasWrite = role.isPrivileged(OrganizationEmployeeModel.PermissionType.WRITE);

        if (targetHasWrite && controller.countEmployeesWithWriteAccess(organizationId) <= 1) {
            controller.deleteOrganization(organizationId);
        } else {
            controller.removeEmployee(organizationId, employeeId);
        }
    }
}
