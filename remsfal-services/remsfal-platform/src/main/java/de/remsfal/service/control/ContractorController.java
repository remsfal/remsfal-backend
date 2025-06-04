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
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Controller for contractor operations.
 */
@RequestScoped
public class ContractorController {

    @Inject
    Logger logger;

    @Inject
    ProjectController projectController;

    @Inject
    UserController userController;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ContractorRepository contractorRepository;

    /**
     * Get contractors for a project.
     *
     * @param user the user
     * @param projectId the project ID
     * @param offset the offset
     * @param limit the limit
     * @return the list of contractors
     */
    public List<ContractorModel> getContractors(final UserModel user, final String projectId, 
                                               final Integer offset, final Integer limit) {
        logger.infov("Retrieving contractors for project (id = {0})", projectId);
        // Verify user has access to the project
        projectController.getProject(user, projectId);
        List<ContractorEntity> entities = contractorRepository.findByProjectId(projectId, offset, limit);
        return new java.util.ArrayList<>(entities);
    }

    /**
     * Count contractors for a project.
     *
     * @param user the user
     * @param projectId the project ID
     * @return the count
     */
    public long countContractors(final UserModel user, final String projectId) {
        // Verify user has access to the project
        projectController.getProject(user, projectId);
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
    public ContractorModel getContractor(final UserModel user, final String projectId, final String contractorId) {
        logger.infov("Retrieving contractor (id = {0}) for project (id = {1})", contractorId, projectId);
        // Verify user has access to the project
        projectController.getProject(user, projectId);
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
    public ContractorModel createContractor(final UserModel user, final String projectId, 
                                           final ContractorModel contractor) {
        logger.infov("Creating contractor for project (id = {0})", projectId);
        // Verify user has access to the project and can write
        // First check if the project exists
        ProjectEntity project = projectRepository.findById(projectId);
        if (project == null) {
            throw new NotFoundException("Project not found");
        }

        // Then check if the user is a member of the project
        ProjectEntity projectEntity = projectRepository.findProjectByUserId(user.getId(), projectId)
                .orElseThrow(() -> new ForbiddenException("User has no membership in this project"));

        if (!projectController.getProjectMemberRole(user, projectId).isPrivileged()) {
            throw new ForbiddenException("Inadequate user rights");
        }

        ContractorEntity entity = new ContractorEntity();
        entity.generateId();
        entity.setProject(projectEntity);
        entity.setCompanyName(contractor.getCompanyName());
        entity.setPhone(contractor.getPhone());
        entity.setEmail(contractor.getEmail());
        entity.setTrade(contractor.getTrade());

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
    public ContractorModel updateContractor(final UserModel user, final String projectId, 
                                           final String contractorId, final ContractorModel contractor) {
        logger.infov("Updating contractor (id = {0}) for project (id = {1})", contractorId, projectId);
        // Verify user has access to the project and can write
        projectController.getProject(user, projectId);

        if (!projectController.getProjectMemberRole(user, projectId).isPrivileged()) {
            throw new ForbiddenException("Inadequate user rights");
        }

        ContractorEntity entity = contractorRepository.findByProjectIdAndContractorId(projectId, contractorId)
                .orElseThrow(() -> new NotFoundException("Contractor not found"));

        if (contractor.getCompanyName() != null) {
            entity.setCompanyName(contractor.getCompanyName());
        }
        if (contractor.getPhone() != null) {
            entity.setPhone(contractor.getPhone());
        }
        if (contractor.getEmail() != null) {
            entity.setEmail(contractor.getEmail());
        }
        if (contractor.getTrade() != null) {
            entity.setTrade(contractor.getTrade());
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
    public boolean deleteContractor(final UserModel user, final String projectId, final String contractorId) {
        logger.infov("Deleting contractor (id = {0}) for project (id = {1})", contractorId, projectId);
        // Verify user has access to the project and can write
        projectController.getProject(user, projectId);

        if (!projectController.getProjectMemberRole(user, projectId).isPrivileged()) {
            throw new ForbiddenException("Inadequate user rights");
        }

        ContractorEntity entity = contractorRepository.findByProjectIdAndContractorId(projectId, contractorId)
                .orElseThrow(() -> new NotFoundException("Contractor not found"));

        return contractorRepository.deleteById(entity.getId());
    }
}
