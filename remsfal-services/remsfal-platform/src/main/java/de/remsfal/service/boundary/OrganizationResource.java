package de.remsfal.service.boundary;

import de.remsfal.core.api.OrganizationEndpoint;
import de.remsfal.core.json.OrganizationJson;
import de.remsfal.core.json.OrganizationListJson;
import de.remsfal.core.model.OrganizationModel;
import de.remsfal.service.boundary.organization.EmployeeResource;
import de.remsfal.service.boundary.organization.OrganizationSubResource;
import de.remsfal.service.entity.dto.OrganizationEntity;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
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
    public OrganizationListJson getOrganizationsOfUser() {
        return OrganizationListJson.valueOf(controller.getOrganizationsOfUser(principal));
    }

    @Override
    public OrganizationListJson getOrganizations(Integer offset, Integer limit) {
        List<OrganizationEntity> organizations = controller.getOrganizations();
        return OrganizationListJson.valueOf(organizations, offset, (long) organizations.size());
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
