package de.remsfal.service.boundary.organization;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.api.organization.EmployeeEndpoint;
import de.remsfal.core.json.organization.OrganizationEmployeeJson;
import de.remsfal.core.json.organization.OrganizationEmployeeListJson;
import de.remsfal.service.control.OrganizationController;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Authenticated
@RequestScoped
public class EmployeeResource extends OrganizationSubResource implements EmployeeEndpoint {

    @Inject
    OrganizationController controller;

    @Inject
    RemsfalPrincipal principal;


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
        System.out.println("checking Permissions...");
        checkWritePermissions(organizationId);
        System.out.println("Permissions checked");
        return OrganizationEmployeeJson.valueOf(
                controller.updateEmployeeRole(organizationId, employeeId, employee.getEmployeeRole())
        );
    }

    @Override
    public void deleteEmployee(UUID organizationId, UUID employeeId) {
        checkWritePermissions(organizationId);
        controller.removeEmployee(organizationId, employeeId);
    }
}
