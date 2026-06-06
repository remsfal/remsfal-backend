package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.OrganizationEmployeeEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
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

    public Optional<OrganizationEmployeeEntity> findOrganizationEmployeeByOrganizationIdAndUserId(UUID organizationId,
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
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<OrganizationEntity> findOrganizationByUserId(final UUID userId, final UUID organizationId) {
        return findOrganizationEmployeeByOrganizationIdAndUserId(organizationId, userId)
                .map(OrganizationEmployeeEntity::getOrganization);
    }

    public void deleteOrganizationEmployeesByOrganizationIdAndUserId(UUID organizationId, UUID userId) {
        getEntityManager()
            .createNamedQuery("OrganizationEmployeeEntity.removeByOrganizationIdAndUserId")
            .setParameter("organizationId", organizationId)
            .setParameter("userId", userId)
            .executeUpdate();
    }

    public OrganizationEmployeeEntity merge(OrganizationEmployeeEntity entity) {
        return getEntityManager().merge(entity);
    }

    public int countEmployeesWithWriteAccess(final UUID organizationId) {
        return getEntityManager()
                .createNamedQuery("OrganizationEmployeeEntity.countEmployeesWithWriteAccess")
                .setParameter("organizationId", organizationId)
                .getResultList().size();
    }

    /**
     * Search organizations by name (case-insensitive, partial match).
     *
     * @param query  the search term (min. 3 characters)
     * @param offset pagination offset
     * @param limit  maximum number of results
     * @return matching organizations
     */
    public List<OrganizationEntity> searchByName(final String query, final int offset, final int limit) {
        return getEntityManager()
            .createQuery("SELECT o FROM OrganizationEntity o WHERE LOWER(o.name) LIKE LOWER(:query)"
                + " ORDER BY o.name", OrganizationEntity.class)
            .setParameter("query", "%" + query + "%")
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    /**
     * Count organizations matching a name search (case-insensitive, partial match).
     *
     * @param query the search term
     * @return total count of matching organizations
     */
    public long countByName(final String query) {
        return getEntityManager()
            .createQuery("SELECT COUNT(o) FROM OrganizationEntity o WHERE LOWER(o.name) LIKE LOWER(:query)",
                Long.class)
            .setParameter("query", "%" + query + "%")
            .getSingleResult();
    }

    /**
     * Find distinct organizations that are contractors in projects accessible to the given user.
     * A project is accessible if the user is either a direct member or an employee of an organization
     * that is linked to the project.
     *
     * @param userId the user ID
     * @param offset pagination offset
     * @param limit  pagination limit
     * @return paginated list of distinct contractor organizations, ordered by name
     */
    public List<OrganizationEntity> findContractorOrganizationsByUser(final UUID userId,
        final int offset, final int limit) {
        return getEntityManager().createQuery(
            "SELECT o FROM OrganizationEntity o WHERE o IN ("
            + "  SELECT DISTINCT c.organization FROM ContractorEntity c"
            + "  WHERE c.organization IS NOT NULL AND ("
            + "    EXISTS (SELECT pm FROM ProjectMembershipEntity pm"
            + "            WHERE pm.project.id = c.project.id AND pm.user.id = :userId)"
            + "    OR EXISTS (SELECT po FROM ProjectOrganizationEntity po"
            + "               JOIN OrganizationEmployeeEntity oe ON oe.organization.id = po.organization.id"
            + "               WHERE po.project.id = c.project.id AND oe.user.id = :userId)"
            + "  )) ORDER BY o.name", OrganizationEntity.class)
            .setParameter("userId", userId)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    /**
     * Count distinct organizations that are contractors in projects accessible to the given user.
     *
     * @param userId the user ID
     * @return count of distinct contractor organizations
     */
    public long countContractorOrganizationsByUser(final UUID userId) {
        return getEntityManager().createQuery(
            "SELECT COUNT(DISTINCT c.organization) FROM ContractorEntity c"
            + " WHERE c.organization IS NOT NULL AND ("
            + "  EXISTS (SELECT pm FROM ProjectMembershipEntity pm"
            + "          WHERE pm.project.id = c.project.id AND pm.user.id = :userId)"
            + "  OR EXISTS (SELECT po FROM ProjectOrganizationEntity po"
            + "             JOIN OrganizationEmployeeEntity oe ON oe.organization.id = po.organization.id"
            + "             WHERE po.project.id = c.project.id AND oe.user.id = :userId)"
            + " )", Long.class)
            .setParameter("userId", userId)
            .getSingleResult();
    }

}
