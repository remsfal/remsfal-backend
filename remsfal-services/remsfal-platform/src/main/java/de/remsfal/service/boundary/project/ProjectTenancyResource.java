package de.remsfal.service.boundary.project;

import java.net.URI;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.remsfal.core.api.project.ProjectTenancyEndpoint;
import de.remsfal.core.json.tenancy.ProjectTenancyListJson;
import de.remsfal.core.json.tenancy.TenancyInfoJson;
import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.service.control.TenancyController;

import java.util.List;

@RequestScoped
public class ProjectTenancyResource extends AbstractProjectResource implements ProjectTenancyEndpoint {

    @Inject
    TenancyController tenancyController;

    @Override
    public TenancyInfoJson getTenancy(final UUID projectId, final UUID tenancyId) {
        checkProjectReadPermissions(projectId);
        final TenancyModel tenancy = tenancyController.getTenancyByProject(projectId, tenancyId);
        return TenancyInfoJson.valueOf(tenancy);
    }

    @Override
    public ProjectTenancyListJson getTenancies(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        final List<? extends TenancyModel> tenancies = tenancyController.getTenanciesByProject(projectId);
        return ProjectTenancyListJson.valueOf(tenancies);
    }

    @Override
    public Response createTenancy(final UUID projectId, final TenancyInfoJson tenancy) {
        checkTenancyWritePermissions(projectId);
        final TenancyModel entity = tenancyController.createTenancy(projectId, tenancy);
        final URI location = uri.getAbsolutePathBuilder().path(String.valueOf(entity.getId())).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(TenancyInfoJson.valueOf(entity))
            .build();
    }

    @Override
    public TenancyInfoJson updateTenancy(final UUID projectId, final UUID tenancyId, final TenancyInfoJson tenancy) {
        checkTenancyWritePermissions(projectId);
        final TenancyModel entity = tenancyController.updateTenancy(projectId, tenancyId, tenancy);
        return TenancyInfoJson.valueOf(entity);
    }

}
