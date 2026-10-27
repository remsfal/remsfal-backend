package de.remsfal.service.boundary.organization;

import de.remsfal.core.api.organization.OrganizationEndpoint;
import de.remsfal.core.json.organization.ClientProjectListJson;
import de.remsfal.core.json.organization.OrganizationEmployeeListJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.organization.OrganizationListJson;
import de.remsfal.core.model.OrganizationModel;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.micrometer.core.annotation.Timed;

import java.util.UUID;

/**
 * @author Miroslaw Keil [miroslaw.keil@student.htw-berlin.de]
 */
@Authenticated
public class OrganizationResource extends OrganizationSubResource implements OrganizationEndpoint {

    @Inject
    Instance<EmployeeResource> employeeResource;

    @Inject
    ResourceContext resourceContext;

    @Override
    @Timed("checks_timer_search_organizations")
    public OrganizationListJson searchOrganizations(final String name, final Integer offset,
        final Integer limit) {
        return OrganizationListJson.valueOf(
            controller.searchOrganizations(name, offset, limit),
            offset,
            controller.countSearchOrganizations(name));
    }

    @Override
    @Timed("checks_timer_get_contractors")
    public OrganizationListJson getContractors(final Integer offset, final Integer limit) {
        return OrganizationListJson.valueOf(
            controller.getContractorOrganizations(principal, offset, limit),
            offset,
            controller.countContractorOrganizations(principal));
    }

    @Override
    @Timed("checks_timer_get_clients")
    public ClientProjectListJson getClients(final UUID organizationId) {
        checkReadPermissions(organizationId);
        return controller.getClients(organizationId);
    }

    @Override
    @Timed("checks_timer_get_organization_employments")
    public OrganizationEmployeeListJson getOrganizationEmployments() {
        return OrganizationEmployeeListJson.valueOfList(controller.getOrganizationEmployments(principal));
    }

    @Override
    @Timed("checks_timer_get_organizations")
    public OrganizationListJson getOrganizations() {
        return OrganizationListJson.valueOf(controller.getOrganizations(principal));
    }

    @Override
    @Timed("checks_timer_create_organization")
    public Response createOrganization(final OrganizationJson organization) {
        OrganizationModel model = controller.createOrganization(organization, principal);
        return getCreatedResponseBuilder(model.getId())
                .type(MediaType.APPLICATION_JSON)
                .entity(OrganizationJson.valueOf(model))
                .build();
    }

    @Override
    @Timed("checks_timer_get_organization")
    public OrganizationJson getOrganization(final UUID organizationId) {
        checkReadPermissions(organizationId);
        return OrganizationJson.valueOf(controller.getOrganizationById(organizationId));
    }

    @Override
    @Timed("checks_timer_update_organization")
    public OrganizationJson updateOrganization(final UUID organizationId, final OrganizationJson organization) {
        checkOwnerPermissions(organizationId);
        return OrganizationJson.valueOf(controller.updateOrganization(principal, organizationId, organization));
    }

    @Override
    @Timed("checks_timer_delete_organization")
    public void deleteOrganization(final UUID organizationId) {
        checkOwnerPermissions(organizationId);
        if (!controller.deleteOrganization(organizationId)) {
            throw new NotFoundException("Organization not found");
        }
    }

    @Override
    @Timed("checks_timer_get_employee_endpoint")
    public EmployeeResource getEmployeeEndpoint() {
        return resourceContext.initResource(employeeResource.get());
    }
}
