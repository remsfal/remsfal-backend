package de.remsfal.service.control;

import de.remsfal.core.model.ContractorModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dao.ContractorRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dto.ContractorEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Controller for contractor operations.
 */
@RequestScoped
public class ContractorController {

    @Inject
    Logger logger;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ContractorRepository contractorRepository;

    @Inject
    ContractorOrganizationLinkController contractorOrganizationLinker;

    @Inject
    AddressController addressController;

    public boolean isContractorEmployee(final UserModel user) {
        logger.infov("Checking whether user (id = {0}) belongs to a contractor-linked organization", user.getId());
        return contractorRepository.existsByOrganizationEmployeeUserId(user.getId());
    }

    /**
     * Get contractors for a project.
     *
     * @param projectId the project ID
     * @param offset the offset
     * @param limit the limit
     * @return the list of contractors
     */
    public List<ContractorEntity> getContractors(final UUID projectId,
        final Integer offset, final Integer limit) {
        logger.infov("Retrieving contractors for project (id = {0})", projectId);
        return contractorRepository.findByProjectId(projectId, offset, limit);
    }

    /**
     * Count contractors for a project.
     *
     * @param user the user
     * @param projectId the project ID
     * @return the count
     */
    public long countContractors(final UserModel user, final UUID projectId) {
        return contractorRepository.countByProjectId(projectId);
    }

    /**
     * Get a contractor.
     *
     * @param user the user
     * @param projectId the project ID
     * @param contractorId the contractor ID
     * @return the contractor
     */
    public ContractorModel getContractor(final UserModel user, final UUID projectId, final UUID contractorId) {
        logger.infov("Retrieving contractor (id = {0}) for project (id = {1})", contractorId, projectId);
        return contractorRepository.findByProjectIdAndContractorId(projectId, contractorId)
            .orElseThrow(() -> new NotFoundException("Contractor not found"));
    }

    /**
     * Create a contractor.
     *
     * @param user the user
     * @param projectId the project ID
     * @param contractor the contractor
     * @return the created contractor
     */
    @Transactional
    public ContractorModel createContractor(final UserModel user, final UUID projectId,
        final ContractorModel contractor) {
        logger.infov("Creating contractor for project (id = {0})", projectId);

        ProjectEntity projectEntity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        ContractorEntity entity = new ContractorEntity();
        entity.generateId();
        entity.setProject(projectEntity);
        entity.setName(contractor.getName());
        entity.setPhone(contractor.getPhone());
        entity.setEmail(contractor.getEmail());
        entity.setTrade(contractor.getTrade());
        entity.setContactPerson(contractor.getContactPerson());
        entity.setRemarks(contractor.getRemarks());

        if (entity.getEmail() != null) {
            contractorOrganizationLinker.relinkByEmail(entity, entity.getEmail());
        }

        if (contractor.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(contractor.getAddress(), null));
        }

        contractorRepository.persistAndFlush(entity);
        return entity;
    }

    /**
     * Update a contractor.
     *
     * @param user the user
     * @param projectId the project ID
     * @param contractorId the contractor ID
     * @param contractor the contractor
     * @return the updated contractor
     */
    @Transactional
    public ContractorModel updateContractor(final UserModel user, final UUID projectId,
        final UUID contractorId, final ContractorModel contractor) {
        logger.infov("Updating contractor (id = {0}) for project (id = {1})", contractorId, projectId);

        ContractorEntity entity = contractorRepository.findByProjectIdAndContractorId(projectId, contractorId)
            .orElseThrow(() -> new NotFoundException("Contractor not found"));

        if (contractor.getName() != null) {
            entity.setName(contractor.getName());
        }
        if (contractor.getPhone() != null) {
            entity.setPhone(contractor.getPhone());
        }
        if (contractor.getEmail() != null) {
            final String normalizedEmail = contractor.getEmail().trim().toLowerCase();
            if (!normalizedEmail.equals(entity.getEmail())) {
                entity.setEmail(normalizedEmail);
                contractorOrganizationLinker.relinkByEmail(entity, normalizedEmail);
            }
        }
        if (contractor.getTrade() != null) {
            entity.setTrade(contractor.getTrade());
        }
        if (contractor.getContactPerson() != null) {
            entity.setContactPerson(contractor.getContactPerson());
        }
        if (contractor.getRemarks() != null) {
            entity.setRemarks(contractor.getRemarks());
        }
        if (contractor.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(contractor.getAddress(), entity.getAddress()));
        }

        return contractorRepository.merge(entity);
    }

    /**
     * Delete a contractor.
     *
     * @param user the user
     * @param projectId the project ID
     * @param contractorId the contractor ID
     * @return true if deleted
     */
    @Transactional
    public boolean deleteContractor(final UserModel user, final UUID projectId, final UUID contractorId) {
        logger.infov("Deleting contractor (id = {0}) for project (id = {1})", contractorId, projectId);
        ContractorEntity entity = contractorRepository.findByProjectIdAndContractorId(projectId, contractorId)
            .orElseThrow(() -> new NotFoundException("Contractor not found"));
        return contractorRepository.deleteById(entity.getId());
    }

}
