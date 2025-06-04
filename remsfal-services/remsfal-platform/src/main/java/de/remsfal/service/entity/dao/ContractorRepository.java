package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.ContractorEntity;
import de.remsfal.service.entity.dto.ContractorEmployeeEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

/**
 * Repository for contractor entities.
 */
@ApplicationScoped
public class ContractorRepository extends AbstractRepository<ContractorEntity> {

    private static final String PARAM_PROJECT_ID = "projectId";
    private static final String PARAM_CONTRACTOR_ID = "contractorId";

    /**
     * Delete a contractor by ID.
     *
     * @param id the contractor ID
     * @return true if the contractor was deleted, false otherwise
     */
    public boolean deleteById(final String id) {
        ContractorEntity entity = getEntityManager().find(ContractorEntity.class, id);
        if (entity != null) {
            getEntityManager().remove(entity);
            getEntityManager().flush();
            return true;
        }
        return false;
    }

    /**
     * Find contractors by project ID.
     *
     * @param projectId the project ID
     * @param offset the offset
     * @param limit the limit
     * @return the list of contractors
     */
    public List<ContractorEntity> findByProjectId(final String projectId, final int offset, final int limit) {
        return getEntityManager().createNamedQuery("ContractorEntity.findByProjectId", ContractorEntity.class)
                .setParameter(PARAM_PROJECT_ID, projectId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * Count contractors by project ID.
     *
     * @param projectId the project ID
     * @return the count
     */
    public long countByProjectId(final String projectId) {
        return getEntityManager()
                .createNamedQuery("ContractorEntity.countByProjectId", Long.class)
                .setParameter(PARAM_PROJECT_ID, projectId)
                .getSingleResult();
    }

    /**
     * Find contractor employees by contractor ID.
     *
     * @param contractorId the contractor ID
     * @return the list of contractor employees
     */
    public List<ContractorEmployeeEntity> findEmployeesByContractorId(final String contractorId) {
        return getEntityManager().createNamedQuery("ContractorEmployeeEntity.findByContractorId", 
                ContractorEmployeeEntity.class)
                .setParameter(PARAM_CONTRACTOR_ID, contractorId)
                .getResultList();
    }

    /**
     * Find a contractor by project ID and contractor ID.
     *
     * @param projectId the project ID
     * @param contractorId the contractor ID
     * @return the optional contractor
     */
    public Optional<ContractorEntity> findByProjectIdAndContractorId(final String projectId, final String contractorId) {
        try {
            // Clear the entity manager cache to ensure we're not getting a cached entity
            getEntityManager().clear();

            ContractorEntity contractor = getEntityManager().find(ContractorEntity.class, contractorId);
            if (contractor != null && contractor.getProjectId().equals(projectId)) {
                return Optional.of(contractor);
            }
            return Optional.empty();
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
