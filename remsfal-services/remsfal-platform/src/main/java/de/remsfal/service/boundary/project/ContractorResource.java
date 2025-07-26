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

import java.net.URI;
import java.util.List;

/**
 * Resource for contractor management.
 */
@RequestScoped
public class ContractorResource extends ProjectSubResource implements ContractorEndpoint {

    @Inject
    ContractorController controller;

    @Override
    public ContractorListJson getContractors(String projectId, Integer offset, Integer limit) {
        checkReadPermissions(projectId);
        List<? extends ContractorModel> contractors = controller.getContractors(projectId, offset, limit);
        return ContractorListJson.valueOf(contractors, offset, controller.countContractors(principal, projectId));
    }

    @Override
    public Response createContractor(String projectId, ContractorJson contractor) {
        checkWritePermissions(projectId);
        ContractorModel model = controller.createContractor(principal, projectId, contractor);
        URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(ContractorJson.valueOf(model))
                .build();
    }

    @Override
    public ContractorJson getContractor(String projectId, String contractorId) {
        checkReadPermissions(projectId);
        try {
            ContractorModel model = controller.getContractor(principal, projectId, contractorId);
            return ContractorJson.valueOf(model);
        } catch (NotFoundException e) {
            throw new NotFoundException("Contractor not found");
        }
    }

    @Override
    public ContractorJson updateContractor(String projectId, String contractorId, ContractorJson contractor) {
        checkWritePermissions(projectId);
        ContractorModel model = controller.updateContractor(principal, projectId, contractorId, contractor);
        return ContractorJson.valueOf(model);
    }

    @Override
    public void deleteContractor(String projectId, String contractorId) {
        checkWritePermissions(projectId);
        if (!controller.deleteContractor(principal, projectId, contractorId)) {
            throw new NotFoundException("Contractor not found");
        }
    }
}