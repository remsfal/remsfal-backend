package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.ContractorEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for contractor entities.
 */
@ApplicationScoped
public class ContractorRepository extends AbstractRepository<ContractorEntity> {

    /**
     * Delete a contractor by ID.
     *
     * @param id the contractor ID
     * @return true if the contractor was deleted, false otherwise
     */
    public boolean deleteById(final UUID id) {
        ContractorEntity entity = getEntityManager().find(ContractorEntity.class, id);
        if (entity != null) {
            getEntityManager().remove(entity);
            getEntityManager().flush();
            return true;
        }
        return false;
    }

    public List<ContractorEntity> findContractorsByEmployee(final UUID employeeId) {
        return find("SELECT c FROM ContractorEntity c JOIN c.employees employee WHERE employee.user.id = :userId",
            Parameters.with(PARAM_USER_ID, employeeId)).list();
    }

    /**
     * Find contractors by project ID.
     *
     * @param projectId the project ID
     * @param offset the offset
     * @param limit the limit
     * @return the list of contractors
     */
    public List<ContractorEntity> findByProjectId(final UUID projectId, final int offset, final int limit) {
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
    public long countByProjectId(final UUID projectId) {
        return getEntityManager()
                .createNamedQuery("ContractorEntity.countByProjectId", Long.class)
                .setParameter(PARAM_PROJECT_ID, projectId)
                .getSingleResult();
    }

    /**
     * Find a contractor by project ID and contractor ID.
     *
     * @param projectId the project ID
     * @param contractorId the contractor ID
     * @return the optional contractor
     */
    public Optional<ContractorEntity> findByProjectIdAndContractorId(
            final UUID projectId, final UUID contractorId) {
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
