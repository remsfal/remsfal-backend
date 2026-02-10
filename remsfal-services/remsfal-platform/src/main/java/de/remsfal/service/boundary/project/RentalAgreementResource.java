package de.remsfal.service.boundary.project;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.remsfal.core.api.project.RentalAgreementEndpoint;
import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.project.RentalAgreementListJson;
import de.remsfal.core.json.project.RentalUnitJson;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.service.control.PropertyController;
import de.remsfal.service.control.RentalAgreementController;

@RequestScoped
public class RentalAgreementResource extends AbstractProjectResource implements RentalAgreementEndpoint {

    @Inject
    RentalAgreementController rentalAgreementController;

    @Inject
    PropertyController propertyController;

    @Override
    public RentalAgreementListJson getRentalAgreements(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        final List<? extends RentalAgreementModel> agreements =
            rentalAgreementController.getRentalAgreementsByProject(projectId);
        final Map<UUID, RentalUnitJson> rentalUnitsMap =
            propertyController.getRentalUnitsMapForProject(projectId);
        return RentalAgreementListJson.valueOf(agreements, rentalUnitsMap);
    }

    @Override
    public RentalAgreementJson getRentalAgreement(final UUID projectId, final UUID agreementId) {
        checkProjectReadPermissions(projectId);
        final RentalAgreementModel agreement =
            rentalAgreementController.getRentalAgreementByProject(projectId, agreementId);
        return RentalAgreementJson.valueOf(agreement);
    }

    @Override
    public Response createRentalAgreement(final UUID projectId, final RentalAgreementJson agreement) {
        checkRentalAgreementWritePermissions(projectId);
        final RentalAgreementModel model = rentalAgreementController.createRentalAgreement(projectId, agreement);
        return getCreatedResponseBuilder(model.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(RentalAgreementJson.valueOf(model))
            .build();
    }

    @Override
    public RentalAgreementJson updateRentalAgreement(final UUID projectId, final UUID agreementId,
            final RentalAgreementJson agreement) {
        checkRentalAgreementWritePermissions(projectId);
        final RentalAgreementModel model =
            rentalAgreementController.updateRentalAgreement(projectId, agreementId, agreement);
        return RentalAgreementJson.valueOf(model);
    }

}
