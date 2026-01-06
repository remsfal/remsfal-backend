package de.remsfal.service.boundary.organization;

import de.remsfal.core.api.organization.EmployeeEndpoint;
import de.remsfal.core.json.organization.OrganizationEmployeeJson;
import de.remsfal.core.json.organization.OrganizationEmployeeListJson;
import de.remsfal.core.model.OrganizationEmployeeModel;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;

import java.util.UUID;

@Authenticated
@RequestScoped
public class EmployeeResource extends OrganizationSubResource implements EmployeeEndpoint {

    @Override
    public OrganizationEmployeeJson getEmployee(UUID organizationId, UUID employeeId) {
        checkReadPermissions(organizationId);
        return OrganizationEmployeeJson.valueOf(controller.getOrganizationEmployee(organizationId, employeeId));
    }

    @Override
    public OrganizationEmployeeListJson getEmployees(UUID organizationId) {
        checkReadPermissions(organizationId);
        return OrganizationEmployeeListJson.valueOfList(controller.getEmployeesByOrganization(organizationId));
    }

    @Override
    public OrganizationEmployeeJson addEmployee(UUID organizationId, OrganizationEmployeeJson employee) {
        checkWritePermissions(organizationId);
        return OrganizationEmployeeJson.valueOf(controller.addEmployee(organizationId, principal, employee));
    }

    @Override
    public OrganizationEmployeeJson updateEmployee(UUID organizationId, UUID employeeId,
        OrganizationEmployeeJson employee) {
        checkWritePermissions(organizationId);
        return OrganizationEmployeeJson.valueOf(
                controller.updateEmployeeRole(organizationId, employeeId, employee.getEmployeeRole())
        );
    }

    @Override
    public void deleteEmployee(UUID organizationId, UUID employeeId) {
        OrganizationEmployeeModel.EmployeeRole role =
            controller.getOrganizationEmployee(organizationId, employeeId).getEmployeeRole();

        if (role == OrganizationEmployeeModel.EmployeeRole.OWNER) {
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
