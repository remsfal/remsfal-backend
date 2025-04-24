package de.remsfal.chat.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import org.jboss.logging.Logger;

import de.remsfal.chat.entity.dao.AuthorizationRepository;
import de.remsfal.chat.entity.dto.ProjectMembershipEntity;
import de.remsfal.chat.entity.dto.UserEntity;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 * @deprecated TODO @Eyad Remove this with issue https://github.com/remsfal/remsfal-backend/issues/315
 */
@Deprecated
@RequestScoped
public class AuthorizationController {

    @Inject
    Logger logger;

    @Inject
    AuthorizationRepository repository;

    public UserEntity getUser(final String userId) {
        logger.infov("Retrieving a user (id = {0})", userId);
        return repository.findUser(userId)
            .orElseThrow(() -> new ForbiddenException("User does not exist"));
    }

    public MemberRole getProjectMemberRole(final UserModel user, final String projectId) {
        logger.infov("Retrieving project member role (user={0}, project={1})", user.getId(), projectId);
        return repository.findMembership(user.getId(), projectId)
            .map(ProjectMembershipEntity::getRole)
            .orElseThrow(() -> new ForbiddenException("Project not exist or user has no membership"));
    }

}
