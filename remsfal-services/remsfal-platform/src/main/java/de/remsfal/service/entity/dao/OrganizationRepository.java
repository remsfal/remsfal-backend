package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.OrganizationEmployeeEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrganizationRepository extends AbstractRepository<OrganizationEntity> {

    public List<OrganizationEmployeeEntity> findOrganizationEmployeesByOrganizationId(UUID organizationId) {
        return getEntityManager()
            .createNamedQuery(
                    "OrganizationEmployeeEntity.findByOrganizationId",
                    OrganizationEmployeeEntity.class
            )
            .setParameter("organizationId", organizationId)
            .getResultList();
    }

    public List<OrganizationEmployeeEntity> findOrganizationEmployeesByUserId(UUID userId) {
        return getEntityManager()
            .createNamedQuery("OrganizationEmployeeEntity.findByUserId", OrganizationEmployeeEntity.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    public Optional<OrganizationEmployeeEntity> findOrganizationEmployeesByOrganizationIdAndUserId(UUID organizationId,
        UUID userId) {
        try {
            return Optional.of(getEntityManager()
                .createNamedQuery(
                        "OrganizationEmployeeEntity.findByOrganizationIdAndUserId",
                        OrganizationEmployeeEntity.class
                )
                .setParameter("organizationId", organizationId)
                .setParameter("userId", userId)
                .getSingleResult());
        }
        catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<OrganizationEntity> findOrganizationByUserId(final UUID userId, final UUID organizationId) {
        return findOrganizationEmployeesByOrganizationIdAndUserId(organizationId, userId)
                .map(OrganizationEmployeeEntity::getOrganization);
    }

    public boolean deleteOrganizationEmployeesByOrganizationIdAndUserId(UUID organizationId, UUID userId) {
        return getEntityManager()
                .createNamedQuery("OrganizationEmployeeEntity.removeByOrganizationIdAndUserId")
                .setParameter("organizationId", organizationId)
                .setParameter("userId", userId)
                .executeUpdate() > 0;
    }

    public OrganizationEmployeeEntity merge(OrganizationEmployeeEntity entity) {
        return getEntityManager().merge(entity);
    }

}
