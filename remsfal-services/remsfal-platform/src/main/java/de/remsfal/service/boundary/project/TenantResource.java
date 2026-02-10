package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.TenantEndpoint;
import de.remsfal.core.json.project.RentalUnitJson;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.json.project.TenantListJson;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.control.PropertyController;
import de.remsfal.service.control.RentalAgreementController;
import de.remsfal.service.control.TenantController;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestScoped
public class TenantResource extends AbstractProjectResource implements TenantEndpoint {

    @Inject
    TenantController controller;

    @Inject
    RentalAgreementController rentalAgreementController;

    @Inject
    PropertyController propertyController;

    @Override
    public TenantListJson getTenants(final UUID projectId) {
        checkProjectReadPermissions(projectId);

        // Load all tenants for the project
        List<TenantModel> tenants = controller.getTenants(projectId);

        // Load rental agreements grouped by tenant ID
        Map<UUID, List<RentalAgreementEntity>> agreementsByTenantRaw =
            rentalAgreementController.getRentalAgreementsByTenant(projectId);

        // Convert to the expected type
        @SuppressWarnings("unchecked")
        Map<UUID, List<? extends RentalAgreementModel>> agreementsByTenant =
            (Map<UUID, List<? extends RentalAgreementModel>>) (Map<UUID, ?>) agreementsByTenantRaw;

        // Load all rental units for the project
        Map<UUID, RentalUnitJson> rentalUnitsMap =
            propertyController.getRentalUnitsMapForProject(projectId);

        return TenantListJson.valueOf(tenants, agreementsByTenant, rentalUnitsMap);
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
}
