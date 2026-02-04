package de.remsfal.service.boundary.project;

import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.remsfal.core.api.project.RentalAgreementEndpoint;
import de.remsfal.core.json.tenancy.ProjectRentalAgreementListJson;
import de.remsfal.core.json.tenancy.RentalAgreementInfoJson;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.service.control.RentalAgreementController;

import java.util.List;

@RequestScoped
public class RentalAgreementResource extends AbstractProjectResource implements RentalAgreementEndpoint {

    @Inject
    RentalAgreementController rentalAgreementController;

    @Override
    public RentalAgreementInfoJson getRentalAgreement(final UUID projectId, final UUID agreementId) {
        checkProjectReadPermissions(projectId);
        final RentalAgreementModel agreement =
            rentalAgreementController.getRentalAgreementByProject(projectId, agreementId);
        return RentalAgreementInfoJson.valueOf(agreement);
    }

    @Override
    public ProjectRentalAgreementListJson getRentalAgreements(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        final List<? extends RentalAgreementModel> agreements =
            rentalAgreementController.getRentalAgreementsByProject(projectId);
        return ProjectRentalAgreementListJson.valueOf(agreements);
    }

    @Override
    public Response createRentalAgreement(final UUID projectId, final RentalAgreementInfoJson agreement) {
        checkRentalAgreementWritePermissions(projectId);
        final RentalAgreementModel model = rentalAgreementController.createRentalAgreement(projectId, agreement);
        return getCreatedResponseBuilder(model.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(RentalAgreementInfoJson.valueOf(model))
            .build();
    }

    @Override
    public RentalAgreementInfoJson updateRentalAgreement(final UUID projectId, final UUID agreementId,
            final RentalAgreementInfoJson agreement) {
        checkRentalAgreementWritePermissions(projectId);
        final RentalAgreementModel model =
            rentalAgreementController.updateRentalAgreement(projectId, agreementId, agreement);
        return RentalAgreementInfoJson.valueOf(model);
    }

}
