package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.ContractorEndpoint;
import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.ContractorListJson;
import de.remsfal.core.model.ContractorModel;
import de.remsfal.service.control.ContractorController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * Resource for contractor management.
 */
@RequestScoped
public class ContractorResource extends AbstractProjectResource implements ContractorEndpoint {

    @Inject
    ContractorController controller;

    @Override
    public ContractorListJson getContractors(final UUID projectId, final Integer offset, final Integer limit) {
        checkProjectReadPermissions(projectId);
        List<? extends ContractorModel> contractors = controller.getContractors(projectId, offset, limit);
        return ContractorListJson.valueOf(contractors, offset, controller.countContractors(principal, projectId));
    }

    @Override
    public Response createContractor(final UUID projectId, final ContractorJson contractor) {
        checkRentalAgreementWritePermissions(projectId);
        ContractorModel model = controller.createContractor(principal, projectId, contractor);
        return getCreatedResponseBuilder(model.getId())
                .type(MediaType.APPLICATION_JSON)
                .entity(ContractorJson.valueOf(model))
                .build();
    }

    @Override
    public ContractorJson getContractor(final UUID projectId, final UUID contractorId) {
        checkProjectReadPermissions(projectId);
        ContractorModel model = controller.getContractor(principal, projectId, contractorId);
        return ContractorJson.valueOf(model);
    }

    @Override
    public ContractorJson updateContractor(final UUID projectId, final UUID contractorId,
        final ContractorJson contractor) {
        checkRentalAgreementWritePermissions(projectId);
        ContractorModel model = controller.updateContractor(principal, projectId, contractorId, contractor);
        return ContractorJson.valueOf(model);
    }

    @Override
    public void deleteContractor(final UUID projectId, final UUID contractorId) {
        checkRentalAgreementWritePermissions(projectId);
        if (!controller.deleteContractor(principal, projectId, contractorId)) {
            throw new NotFoundException("Contractor not found");
        }
    }
}