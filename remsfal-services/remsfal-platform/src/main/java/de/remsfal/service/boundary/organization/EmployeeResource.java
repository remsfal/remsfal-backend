package de.remsfal.service.boundary.organization;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.api.EmployeeEndpoint;
import de.remsfal.core.json.OrganizationEmployeeJson;
import de.remsfal.core.json.OrganizationEmployeeListJson;
import de.remsfal.service.control.OrganizationController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@RequestScoped
public class EmployeeResource implements EmployeeEndpoint {

    @Inject
    OrganizationController controller;

    @Inject
    RemsfalPrincipal principal;


    @Override
    public OrganizationEmployeeListJson getEmployees(UUID organizationId) {
        return OrganizationEmployeeListJson.valueOfList(controller.getEmployeesByOrganization(organizationId));
    }

    @Override
    public OrganizationEmployeeJson addEmployee(UUID organizationId, OrganizationEmployeeJson employee) {
        return OrganizationEmployeeJson.valueOf(controller.addEmployee(organizationId, principal, employee));
    }

    @Override
    public OrganizationEmployeeJson updateEmployee(UUID organizationId, UUID employeeId, OrganizationEmployeeJson employee) {
        return OrganizationEmployeeJson.valueOf(controller.updateEmployeeRole(organizationId, employeeId, employee.getEmployeeRole()));
    }

    @Override
    public void deleteEmployee(UUID organizationId, UUID employeeId) {
        controller.removeEmployee(organizationId, employeeId);
    }
}
