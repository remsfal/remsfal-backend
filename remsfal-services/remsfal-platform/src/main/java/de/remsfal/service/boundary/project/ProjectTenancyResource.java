package de.remsfal.service.boundary.project;

import java.net.URI;
import java.util.UUID;

import de.remsfal.core.api.project.ProjectTenancyEndpoint;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.remsfal.core.json.tenancy.ProjectTenancyListJson;
import de.remsfal.core.json.tenancy.TenancyInfoJson;
import de.remsfal.service.control.TenancyController;
import de.remsfal.service.entity.dto.TenancyEntity;

import java.util.List;

import jakarta.transaction.Transactional; 

@RequestScoped
@Transactional
public class ProjectTenancyResource extends ProjectSubResource implements ProjectTenancyEndpoint {

    @Inject
    TenancyController tenancyController;

    @Override
    public TenancyInfoJson getTenancy(final UUID projectId, final UUID tenancyId) {
        checkReadPermissions(projectId);
        final TenancyEntity tenancy = tenancyController.getTenancyByProject(projectId, tenancyId);
        return TenancyInfoJson.valueOf(tenancy);
    }

    @Override
    public ProjectTenancyListJson getTenancies(final UUID projectId) {
        checkReadPermissions(projectId);
        final List<TenancyEntity> tenancies = tenancyController.getTenanciesByProject(projectId);
        return ProjectTenancyListJson.valueOf(tenancies);
    }

    @Override
    public Response createTenancy(final UUID projectId, final TenancyInfoJson tenancy) {
        checkWritePermissions(projectId);
        final TenancyEntity entity = tenancyController.createTenancy(projectId, tenancy);
        final URI location = uri.getAbsolutePathBuilder().path(String.valueOf(entity.getId())).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(TenancyInfoJson.valueOf(entity))
            .build();
    }

    @Override
    public TenancyInfoJson updateTenancy(final UUID projectId, final UUID tenancyId, final TenancyInfoJson tenancy) {
        checkWritePermissions(projectId);
        final TenancyEntity entity = tenancyController.updateTenancy(projectId, tenancyId, tenancy);
        return TenancyInfoJson.valueOf(entity);
    }

}
