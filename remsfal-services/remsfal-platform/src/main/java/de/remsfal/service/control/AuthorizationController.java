package de.remsfal.service.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

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
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
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
        List<ProjectMembershipEntity> memberships = projectRepository.findMembershipByUserId(userId, 0,
            Integer.MAX_VALUE);
        return memberships.stream().collect(Collectors.toMap(
            m -> m.getProject().getId().toString(),
            m -> m.getRole().name()
        ));
    }

    public Map<String, String> getTenancyAuthorization(final UUID userId) {
        List<TenancyEntity> tenancies = tenancyRepository.findTenanciesByTenant(userId);
        return tenancies.stream().collect(Collectors.toMap(
            t -> t.getId().toString(),
            t -> t.getProjectId().toString()
        ));
    }

}
