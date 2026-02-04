package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.TenantEndpoint;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.CustomerModel;
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
    public Response createTenant(final UUID projectId, final UserJson tenant) {
        checkRentalAgreementWritePermissions(projectId);
        final CustomerModel model = controller.createTenant(projectId, tenant);
        return getCreatedResponseBuilder(model.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(UserJson.valueOf(model))
            .build();
    }

    @Override
    public List<UserJson> getTenants(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        List<CustomerModel> tenants = controller.getTenants(projectId);
        return tenants.stream()
            .map(UserJson::valueOf)
            .collect(Collectors.toList());
    }

    @Override
    public UserJson getTenant(final UUID projectId, final UUID tenantId) {
        checkProjectReadPermissions(projectId);
        final CustomerModel model = controller.getTenant(projectId, tenantId);
        return UserJson.valueOf(model);
    }

    @Override
    public UserJson updateTenant(final UUID projectId, final UUID tenantId, UserJson tenant) {
        checkRentalAgreementWritePermissions(projectId);
        final CustomerModel model = controller.updateTenant(projectId, tenantId, tenant);
        return UserJson.valueOf(model);
    }

    @Override
    public Response deleteTenant(UUID projectId, UUID tenantId) {
        checkRentalAgreementWritePermissions(projectId);
        controller.deleteTenant(projectId, tenantId);
        return Response.noContent().build();
    }
}
