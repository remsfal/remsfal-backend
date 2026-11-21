package de.remsfal.service.entity.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import jakarta.persistence.NoResultException;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ProjectRepository extends AbstractRepository<ProjectEntity> {

    private static final String PARAM_ROLE = "role";

    /**
     * JPQL condition: the user is a direct project member or an employee of an organization that is a project member.
     */
    private static final String ACCESSIBLE_BY_USER = "(EXISTS (SELECT m FROM ProjectMembershipEntity m "
        + "WHERE m.project = p AND m.user.id = :userId) "
        + "OR EXISTS (SELECT po FROM ProjectOrganizationEntity po, OrganizationEmployeeEntity oe "
        + "WHERE po.project = p AND oe.organization = po.organization AND oe.user.id = :userId))";

    public List<ProjectMembershipEntity> findMembershipByUserId(final UUID userId,
        final int offset, final int limit) {
        return getEntityManager().createNamedQuery("ProjectMembershipEntity.findByUserId",
            ProjectMembershipEntity.class)
            .setParameter(PARAM_USER_ID, userId)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    public long countMembershipByUserId(final UUID userId) {
        return getEntityManager()
            .createNamedQuery("ProjectMembershipEntity.countByUserId", Long.class)
            .setParameter(PARAM_USER_ID, userId)
            .getSingleResult();
    }

    public Optional<ProjectMembershipEntity> findMembershipByUserIdAndProjectId(final UUID userId,
        final UUID projectId) {
        try {
            return Optional.of(getEntityManager()
                .createNamedQuery("ProjectMembershipEntity.findByProjectIdAndUserId",
                    ProjectMembershipEntity.class)
                .setParameter(PARAM_PROJECT_ID, projectId)
                .setParameter(PARAM_USER_ID, userId)
                .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<ProjectEntity> findProjectByUserId(final UUID userId, final UUID projectId) {
        return findMembershipByUserIdAndProjectId(userId, projectId).map(ProjectMembershipEntity::getProject);
    }

    public boolean removeMembershipByUserIdAndProjectId(final UUID userId, final UUID projectId) {
        return getEntityManager()
            .createNamedQuery("ProjectMembershipEntity.removeByProjectIdAndUserId")
            .setParameter(PARAM_PROJECT_ID, projectId)
            .setParameter(PARAM_USER_ID, userId)
            .executeUpdate() > 0;
    }

    public ProjectMembershipEntity merge(final ProjectMembershipEntity entity) {
        return getEntityManager().merge(entity);
    }

    /**
     * Find a project the user can access, either directly or through one of the user's organizations.
     */
    public Optional<ProjectEntity> findAccessibleProject(final UUID userId, final UUID projectId) {
        return getEntityManager()
            .createQuery("SELECT p FROM ProjectEntity p WHERE p.id = :projectId AND " + ACCESSIBLE_BY_USER,
                ProjectEntity.class)
            .setParameter(PARAM_PROJECT_ID, projectId)
            .setParameter(PARAM_USER_ID, userId)
            .getResultStream()
            .findFirst();
    }

    /**
     * Find all projects the user can access, either directly or through one of the user's organizations.
     */
    public List<ProjectEntity> findAccessibleProjects(final UUID userId, final int offset, final int limit) {
        return getEntityManager()
            .createQuery("SELECT p FROM ProjectEntity p WHERE " + ACCESSIBLE_BY_USER + " ORDER BY p.title, p.id",
                ProjectEntity.class)
            .setParameter(PARAM_USER_ID, userId)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    public long countAccessibleProjects(final UUID userId) {
        return getEntityManager()
            .createQuery("SELECT count(p) FROM ProjectEntity p WHERE " + ACCESSIBLE_BY_USER, Long.class)
            .setParameter(PARAM_USER_ID, userId)
            .getSingleResult();
    }

    /**
     * Find all projects in which the user is a direct member with the given role.
     */
    public List<ProjectEntity> findProjectsByUserIdAndRole(final UUID userId, final MemberRole role) {
        return getEntityManager()
            .createQuery("SELECT m.project FROM ProjectMembershipEntity m "
                + "WHERE m.user.id = :userId AND m.role = :role", ProjectEntity.class)
            .setParameter(PARAM_USER_ID, userId)
            .setParameter(PARAM_ROLE, role)
            .getResultList();
    }

    /**
     * Find all projects in which the organization is a member with the given role.
     */
    public List<ProjectEntity> findProjectsByOrganizationIdAndRole(final UUID organizationId, final MemberRole role) {
        return getEntityManager()
            .createQuery("SELECT po.project FROM ProjectOrganizationEntity po "
                + "WHERE po.organization.id = :organizationId AND po.role = :role", ProjectEntity.class)
            .setParameter(PARAM_ORGANIZATION_ID, organizationId)
            .setParameter(PARAM_ROLE, role)
            .getResultList();
    }

    /**
     * Removes the organization as managing organization from all projects it manages with a bulk update, which does
     * not touch the project entities in the persistence context.
     *
     * @return ids of the projects that were managed by the organization
     */
    public List<UUID> clearManagingOrganization(final UUID organizationId) {
        final List<UUID> projectIds = getEntityManager()
            .createQuery("SELECT p.id FROM ProjectEntity p WHERE p.managingOrganization.id = :organizationId",
                UUID.class)
            .setParameter(PARAM_ORGANIZATION_ID, organizationId)
            .getResultList();
        if (!projectIds.isEmpty()) {
            getEntityManager()
                .createQuery("UPDATE ProjectEntity p SET p.managingOrganization = null "
                    + "WHERE p.managingOrganization.id = :organizationId")
                .setParameter(PARAM_ORGANIZATION_ID, organizationId)
                .executeUpdate();
        }
        return projectIds;
    }

}
