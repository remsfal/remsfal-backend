package de.remsfal.service.boundary.project;

import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.remsfal.core.api.project.RentalAgreementEndpoint;
import de.remsfal.core.json.tenancy.ProjectTenancyListJson;
import de.remsfal.core.json.tenancy.TenancyInfoJson;
import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.service.control.TenancyController;

import java.util.List;

@RequestScoped
public class RentalAgreementResource extends AbstractProjectResource implements RentalAgreementEndpoint {

    @Inject
    TenancyController tenancyController;

    @Override
    public TenancyInfoJson getRentalAgreement(final UUID projectId, final UUID agreementId) {
        checkProjectReadPermissions(projectId);
        final TenancyModel tenancy = tenancyController.getTenancyByProject(projectId, agreementId);
        return TenancyInfoJson.valueOf(tenancy);
    }

    @Override
    public ProjectTenancyListJson getRentalAgreements(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        final List<? extends TenancyModel> tenancies = tenancyController.getTenanciesByProject(projectId);
        return ProjectTenancyListJson.valueOf(tenancies);
    }

    @Override
    public Response createRentalAgreement(final UUID projectId, final TenancyInfoJson tenancy) {
        checkTenancyWritePermissions(projectId);
        final TenancyModel model = tenancyController.createTenancy(projectId, tenancy);
        return getCreatedResponseBuilder(model.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(TenancyInfoJson.valueOf(model))
            .build();
    }

    @Override
    public TenancyInfoJson updateRentalAgreement(final UUID projectId, final UUID agreementId, final TenancyInfoJson tenancy) {
        checkTenancyWritePermissions(projectId);
        final TenancyModel model = tenancyController.updateTenancy(projectId, agreementId, tenancy);
        return TenancyInfoJson.valueOf(model);
    }

}
