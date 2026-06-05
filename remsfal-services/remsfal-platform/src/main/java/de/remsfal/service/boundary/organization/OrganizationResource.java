package de.remsfal.service.boundary.organization;

import de.remsfal.core.api.organization.OrganizationEndpoint;
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
    public OrganizationListJson searchOrganizations(final String name, final Integer offset,
        final Integer limit) {
        return OrganizationListJson.valueOf(
            controller.searchOrganizations(name, offset, limit),
            offset,
            controller.countSearchOrganizations(name));
    }

    @Override
    public OrganizationListJson getContractors(final Integer offset, final Integer limit) {
        return OrganizationListJson.valueOf(
            controller.getContractorOrganizations(principal, offset, limit),
            offset,
            controller.countContractorOrganizations(principal));
    }

    @Override
    public OrganizationEmployeeListJson getOrganizationEmployments() {
        return OrganizationEmployeeListJson.valueOfList(controller.getOrganizationEmployments(principal));
    }

    @Override
    public OrganizationListJson getOrganizations() {
        return OrganizationListJson.valueOf(controller.getOrganizations(principal));
    }

    @Override
    public Response createOrganization(OrganizationJson organization) {
        OrganizationModel model = controller.createOrganization(organization, principal);
        return getCreatedResponseBuilder(model.getId())
                .type(MediaType.APPLICATION_JSON)
                .entity(OrganizationJson.valueOf(model))
                .build();
    }

    @Override
    public OrganizationJson getOrganization(UUID organizationId) {
        checkReadPermissions(organizationId);
        return OrganizationJson.valueOf(controller.getOrganizationById(organizationId));
    }

    @Override
    public OrganizationJson updateOrganization(UUID organizationId, OrganizationJson organization) {
        checkWritePermissions(organizationId);
        return OrganizationJson.valueOf(controller.updateOrganization(principal, organizationId, organization));
    }

    @Override
    public void deleteOrganization(UUID organizationId) {
        checkOwnerPermissions(organizationId);
        if (!controller.deleteOrganization(organizationId)) {
            throw new NotFoundException("Organization not found");
        }
    }

    @Override
    public EmployeeResource getEmployeeEndpoint() {
        return resourceContext.initResource(employeeResource.get());
    }
}
