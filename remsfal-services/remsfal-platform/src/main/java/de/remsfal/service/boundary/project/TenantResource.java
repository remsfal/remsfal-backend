package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.TenantEndpoint;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.control.TenantController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestScoped
public class TenantResource extends AbstractProjectResource implements TenantEndpoint {

    @Inject
    TenantController controller;

    @Override
    public Response createTenant(final UUID projectId, final TenantJson tenant) {
        checkRentalAgreementWritePermissions(projectId);
        final TenantModel model = controller.createTenant(projectId, tenant);
        return getCreatedResponseBuilder(model.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(TenantJson.valueOf(model))
            .build();
    }

    @Override
    public List<TenantJson> getTenants(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        List<TenantModel> tenants = controller.getTenants(projectId);
        return tenants.stream()
            .map(TenantJson::valueOf)
            .collect(Collectors.toList());
    }

    @Override
    public TenantJson getTenant(final UUID projectId, final UUID tenantId) {
        checkProjectReadPermissions(projectId);
        final TenantModel model = controller.getTenant(projectId, tenantId);
        return TenantJson.valueOf(model);
    }

    @Override
    public TenantJson updateTenant(final UUID projectId, final UUID tenantId, TenantJson tenant) {
        checkRentalAgreementWritePermissions(projectId);
        final TenantModel model = controller.updateTenant(projectId, tenantId, tenant);
        return TenantJson.valueOf(model);
    }

    @Override
    public Response deleteTenant(UUID projectId, UUID tenantId) {
        checkRentalAgreementWritePermissions(projectId);
        controller.deleteTenant(projectId, tenantId);
        return Response.noContent().build();
    }
}
