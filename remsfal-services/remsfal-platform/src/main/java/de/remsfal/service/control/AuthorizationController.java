package de.remsfal.service.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.core.model.UserAuthenticationModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.control.event.AuthenticationEvent;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.TenancyRepository;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.service.entity.dto.OrganizationEmployeeEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.ProjectOrganizationEntity;
import de.remsfal.service.entity.dto.TenancyEntity;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import de.remsfal.service.entity.dto.UserEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class AuthorizationController {
    
    @Inject
    Logger logger;

    @Inject
    UserRepository userRepository;

    @Inject
    UserController userController;

    @Inject
    UserAuthenticationRepository userAuthRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    TenancyRepository tenancyRepository;

    @Inject
    de.remsfal.service.entity.dao.OrganizationRepository organizationRepository;

    @Inject
    de.remsfal.service.entity.dao.ProjectOrganizationRepository projectOrganizationRepository;

    @Inject
    private Event<AuthenticationEvent> authenticatedUser;

    public UserModel getAuthenticatedUser(final UUID userId) {
        logger.infov("Retrieving authenticated user (id = {0})", userId);
        return userAuthRepository.findByUserId(userId)
            .orElseThrow(() -> new UnauthorizedException("User does not exist"));
    }

    @Transactional
    public UserModel authenticateUser(final String googleId, final String email) {
        logger.infov("Authenticating a user (googleId={0}, email={1})", googleId, email);

        final Optional<UserEntity> entity = userRepository.findByTokenId(googleId);
        if(entity.isPresent()) {
            authenticatedUser.fireAsync(new AuthenticationEvent(googleId, email));
            return entity.get();
        }

        return userController.createUser(googleId, email);
    }
    
    public void onPrincipalAuthentication(@ObservesAsync final AuthenticationEvent event) {
        logger.infov("Updating authentication timestamp of user (googleId={0}, email={1})",
            event.getGoogleId(), event.getEmail());
        try {
            userRepository.updateAuthenticatedAt(event.getGoogleId(), event.getAuthenticatedAt());
        } catch (Exception e) {
            logger.error("Unable to update authentication timestamp", e);
        }
    }

    /** Checks if there is an existing user authentication entity for the given user ID */
    public boolean hasUserRefreshToken(final UUID userId) {
        return userAuthRepository.findByUserId(userId).isEmpty();
    }

    /** Creates a new user authentication entity with the given user ID and refresh token */
    public void createRefreshToken(final UUID userId, final UUID refreshTokenId) {
        UserAuthenticationEntity userAuthenticationEntity = new UserAuthenticationEntity();
        userRepository.findByIdOptional(userId).ifPresentOrElse(userAuthenticationEntity::setUser,
            () -> {
                throw new UnauthorizedException("User not found: " + userId);
            });
        userAuthenticationEntity.setRefreshTokenId(refreshTokenId);
        userAuthRepository.persist(userAuthenticationEntity);
    }

    /** Updates the existing refresh token for the given user ID */
    public void updateExistingRefreshToken(final UUID userId, final UUID refreshTokenId) {
        userAuthRepository.updateRefreshTokenId(userId, refreshTokenId);
    }

    /** Validates the refresh token claim against the stored refresh token for the given user ID */
    public UserAuthenticationModel requireValidRefreshToken(final UUID userId, final UUID refreshTokenId) {
        Optional<UserAuthenticationEntity> userAuth = userAuthRepository.findByUserId(userId);

        if (userAuth.isEmpty()) {
            throw new UnauthorizedException("User not found: " + userId);
        }

        UUID saved = userAuth.get().getRefreshTokenId();
        if (saved == null || !saved.equals(refreshTokenId)) {
            throw new UnauthorizedException("Refresh token mismatch.");
        }

        return userAuth.get();
    }

    public void deleteRefreshToken(final UUID userId) {
        userAuthRepository.deleteByUserId(userId);
    }

    public Map<String, String> getProjectAuthorization(final UUID userId) {
        Map<UUID, MemberRole> projectRoles = new HashMap<>();

        // 1. Add direct project memberships
        List<ProjectMembershipEntity> directMemberships = projectRepository.findMembershipByUserId(userId, 0,
            Integer.MAX_VALUE);
        for (ProjectMembershipEntity membership : directMemberships) {
            projectRoles.put(membership.getProject().getId(), membership.getRole());
        }

        // 2. Add indirect memberships through organizations
        List<ProjectOrganizationEntity> orgProjects = projectOrganizationRepository.findByUserId(userId);
        List<OrganizationEmployeeEntity> userOrgs = organizationRepository.findOrganizationEmployeesByUserId(userId);

        // Create a map of organization ID to employee role for quick lookup
        Map<UUID, EmployeeRole> orgRoles = userOrgs.stream()
            .collect(Collectors.toMap(
                e -> e.getOrganization().getId(),
                OrganizationEmployeeEntity::getRole
            ));

        for (ProjectOrganizationEntity orgProject : orgProjects) {
            UUID projectId = orgProject.getProject().getId();
            MemberRole orgRoleInProject = orgProject.getRole();
            EmployeeRole userRoleInOrg = orgRoles.get(orgProject.getOrganization().getId());

            if (userRoleInOrg != null) {
                MemberRole derivedRole = calculateProjectRole(userRoleInOrg, orgRoleInProject);

                // Keep the highest role (lowest leadership value) for this project
                projectRoles.merge(projectId, derivedRole,
                    (existing, derived) -> existing.getLeadershipLevel() <= derived.getLeadershipLevel()
                        ? existing : derived);
            }
        }

        // Convert to String map
        return projectRoles.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().name()
            ));
    }

    /**
     * Calculates the project role based on the user's role in the organization
     * and the organization's role in the project, according to Authorization.md.
     *
     * @param employeeRole User's role in the organization
     * @param orgRoleInProject Organization's role in the project
     * @return The calculated project role for the user
     */
    private MemberRole calculateProjectRole(EmployeeRole employeeRole, MemberRole orgRoleInProject) {
        return switch (employeeRole) {
            case OWNER -> orgRoleInProject;  // Owner gets the organization's role
            case MANAGER -> {
                // Manager gets MANAGER or lower (higher leadership value)
                if (orgRoleInProject.getLeadershipLevel() <= MemberRole.MANAGER.getLeadershipLevel()) {
                    yield MemberRole.MANAGER;
                } else {
                    yield orgRoleInProject;
                }
            }
            case STAFF -> {
                // Staff gets STAFF or lower (higher leadership value)
                if (orgRoleInProject.getLeadershipLevel() <= MemberRole.STAFF.getLeadershipLevel()) {
                    yield MemberRole.STAFF;
                } else {
                    yield orgRoleInProject;
                }
            }
        };
    }

    public Map<String, String> getTenancyAuthorization(final UUID userId) {
        List<TenancyEntity> tenancies = tenancyRepository.findTenanciesByTenant(userId);
        return tenancies.stream().collect(Collectors.toMap(
            t -> t.getId().toString(),
            t -> t.getProjectId().toString()
        ));
    }

    public Map<String, String> getOrganizationAuthorization(final UUID userId) {
        List<de.remsfal.service.entity.dto.OrganizationEmployeeEntity> employees =
            organizationRepository.findOrganizationEmployeesByUserId(userId);
        return employees.stream().collect(Collectors.toMap(
            e -> e.getOrganization().getId().toString(),
            e -> e.getRole().name()
        ));
    }

}
