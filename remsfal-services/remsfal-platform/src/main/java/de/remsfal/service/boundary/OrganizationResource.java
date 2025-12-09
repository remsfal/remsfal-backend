package de.remsfal.service.boundary;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.api.OrganizationEndpoint;
import de.remsfal.core.json.OrganizationJson;
import de.remsfal.core.json.OrganizationListJson;
import de.remsfal.core.model.OrganizationModel;
import de.remsfal.service.boundary.organization.EmployeeResource;
import de.remsfal.service.boundary.organization.OrganizationSubResource;
import de.remsfal.service.control.OrganizationController;
import de.remsfal.service.entity.dto.OrganizationEntity;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Authenticated
public class OrganizationResource extends OrganizationSubResource implements OrganizationEndpoint {

    @Inject
    OrganizationController controller;

    @Context
    protected UriInfo uri;

    @Inject
    Instance<EmployeeResource> employeeResource;
    @Inject
    ResourceContext resourceContext;

    @Inject
    RemsfalPrincipal principal;

    //TODO: Implement permission checker

    @Override
    public OrganizationJson getOrganization(UUID organizationId) {
        try {
            return OrganizationJson.valueOf(controller.getOrganizationById(organizationId));
        } catch (NotFoundException e) {
            throw new NotFoundException("Organization not found");
        }
    }


    //TODO: Implement permission checker

    @Override
    public OrganizationListJson getOrganizations(Integer offset, Integer limit) {
        List<OrganizationEntity> organizations = controller.getOrganizations();
        return OrganizationListJson.valueOf(organizations, offset, (long) organizations.size());
    }

    @Override
    public Response createOrganization(OrganizationJson organization) {
        OrganizationModel organizationModel = controller.createOrganization(organization);
        URI location = uri.getAbsolutePathBuilder().path(organizationModel.getId().toString()).build();
        return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(OrganizationJson.valueOf(organizationModel))
                .build();
    }

    //TODO: Implement permission checker

    @Override
    public OrganizationJson updateOrganization(UUID organizationId, OrganizationJson organization) {
        checkWritePermissions(organizationId);
        return OrganizationJson.valueOf(controller.updateOrganization(principal, organizationId, organization));
    }

    //TODO: Implement permission checker

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
